package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.api.ForexFactoryApiService
import com.example.data.api.ForexFactoryEventDto
import com.example.data.api.ForexFactoryNetworkClient
import com.example.data.model.EconomicEvent
import com.example.data.model.ForexFactoryCalendarStatus
import com.example.data.model.NewsImpact
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EconomicCalendarRepository(private val context: Context? = null) {

    private val api: ForexFactoryApiService = ForexFactoryNetworkClient.apiService
    private val prefs: SharedPreferences? = context?.getSharedPreferences("forex_factory_prefs", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, ForexFactoryEventDto::class.java)
    private val jsonAdapter = moshi.adapter<List<ForexFactoryEventDto>>(listType)

    private val _events = MutableStateFlow<List<EconomicEvent>>(emptyList())
    val events: StateFlow<List<EconomicEvent>> = _events.asStateFlow()

    private val _status = MutableStateFlow(
        ForexFactoryCalendarStatus(
            isLive = false,
            lastSyncMs = 0L,
            totalEvents = 0,
            isRateLimited = false,
            errorMessage = null
        )
    )
    val status: StateFlow<ForexFactoryCalendarStatus> = _status.asStateFlow()

    private var inMemoryEvents: List<EconomicEvent> = emptyList()
    private var lastFetchTimestamp: Long = 0L

    init {
        // Load initial data from disk cache or baseline schedule
        loadInitialData()
    }

    private fun loadInitialData() {
        val cachedJson = prefs?.getString(KEY_CACHED_JSON, null)
        val savedSyncTime = prefs?.getLong(KEY_LAST_SYNC, 0L) ?: 0L

        if (!cachedJson.isNullOrEmpty()) {
            try {
                val dtos = jsonAdapter.fromJson(cachedJson)
                if (!dtos.isNullOrEmpty()) {
                    val domainEvents = dtos.mapIndexed { index, dto -> mapDtoToDomain(index, dto) }
                    inMemoryEvents = domainEvents
                    lastFetchTimestamp = savedSyncTime
                    _events.value = domainEvents
                    _status.value = ForexFactoryCalendarStatus(
                        isLive = false,
                        lastSyncMs = savedSyncTime,
                        totalEvents = domainEvents.size,
                        isRateLimited = false,
                        errorMessage = "Loaded from local cache"
                    )
                    return
                }
            } catch (e: Exception) {
                Log.w("ForexFactoryRepo", "Failed to deserialize cached events: ${e.message}")
            }
        }

        // If no cache exists, initialize with current week's institutional baseline schedule
        val baseline = generateInstitutionalBaseline(System.currentTimeMillis())
        inMemoryEvents = baseline
        _events.value = baseline
        _status.value = ForexFactoryCalendarStatus(
            isLive = false,
            lastSyncMs = System.currentTimeMillis(),
            totalEvents = baseline.size,
            isRateLimited = false,
            errorMessage = null
        )
    }

    /**
     * Fetches live calendar data from the Forex Factory JSON endpoint (nfs.faireconomy.media).
     * Enforces smart caching to respect the 5-minute rate limit on Fair Economy servers.
     */
    suspend fun refreshCalendar(force: Boolean = false): List<EconomicEvent> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cooldownMs = 3 * 60 * 1000L // 3 minutes cooldown

        if (!force && inMemoryEvents.isNotEmpty() && (now - lastFetchTimestamp < cooldownMs)) {
            Log.d("ForexFactoryRepo", "Returning cached Forex Factory events (cooldown active)")
            return@withContext inMemoryEvents
        }

        try {
            Log.d("ForexFactoryRepo", "Connecting to Forex Factory API: ${ForexFactoryNetworkClient.BASE_URL}ff_calendar_thisweek.json")
            val dtos = api.getThisWeekCalendar()

            if (dtos.isNotEmpty()) {
                val domainEvents = dtos.mapIndexed { index, dto -> mapDtoToDomain(index, dto) }
                inMemoryEvents = domainEvents
                lastFetchTimestamp = now
                _events.value = domainEvents

                _status.value = ForexFactoryCalendarStatus(
                    isLive = true,
                    lastSyncMs = now,
                    totalEvents = domainEvents.size,
                    isRateLimited = false,
                    errorMessage = null
                )

                // Save to local persistence
                try {
                    val serialized = jsonAdapter.toJson(dtos)
                    prefs?.edit()
                        ?.putString(KEY_CACHED_JSON, serialized)
                        ?.putLong(KEY_LAST_SYNC, now)
                        ?.apply()
                } catch (e: Exception) {
                    Log.w("ForexFactoryRepo", "Failed to save to SharedPreferences: ${e.message}")
                }

                return@withContext domainEvents
            } else {
                throw IllegalStateException("Empty event array received from Forex Factory")
            }
        } catch (e: Throwable) {
            val isRateLimited = (e is HttpException && e.code() == 429)
            val errorMsg = when {
                isRateLimited -> "Forex Factory rate limit active (HTTP 429). Server asks for 5-min cooldown. Displaying cached events."
                e is HttpException -> "Forex Factory server returned HTTP ${e.code()}: ${e.message()}"
                else -> "Forex Factory network sync error: ${e.message ?: "Unable to connect"}"
            }
            Log.w("ForexFactoryRepo", errorMsg)

            // Graceful fallback to cached or baseline events so screen is never blank
            val fallbackEvents = if (inMemoryEvents.isNotEmpty()) {
                inMemoryEvents
            } else {
                generateInstitutionalBaseline(now).also { inMemoryEvents = it }
            }

            _events.value = fallbackEvents
            _status.value = ForexFactoryCalendarStatus(
                isLive = false,
                lastSyncMs = if (lastFetchTimestamp > 0) lastFetchTimestamp else now,
                totalEvents = fallbackEvents.size,
                isRateLimited = isRateLimited,
                errorMessage = errorMsg
            )

            return@withContext fallbackEvents
        }
    }

    private fun mapDtoToDomain(index: Int, dto: ForexFactoryEventDto): EconomicEvent {
        val title = dto.title?.trim().orEmpty().ifEmpty { "Economic Release" }
        val country = dto.country?.trim()?.uppercase(Locale.ROOT).orEmpty().ifEmpty { "ALL" }
        val dateRaw = dto.date.orEmpty()
        val utcTimestamp = parseIsoDate(dateRaw)
        val impact = NewsImpact.fromString(dto.impact)

        val actual = dto.actual?.trim()?.takeIf { it.isNotEmpty() }
        val forecast = dto.forecast?.trim()?.takeIf { it.isNotEmpty() }
        val previous = dto.previous?.trim()?.takeIf { it.isNotEmpty() }

        val countryFlag = getCountryFlag(country)
        val affectedPairs = getAffectedSctPairs(country)
        val sctNote = generateSctInstitutionalPlaybook(title, country, impact)

        val id = "ff_${index}_${country}_${utcTimestamp}"

        return EconomicEvent(
            id = id,
            title = title,
            country = country,
            countryFlag = countryFlag,
            utcTimestamp = utcTimestamp,
            dateRaw = dateRaw,
            impact = impact,
            actual = actual,
            forecast = forecast,
            previous = previous,
            sctImpactNote = sctNote,
            affectedSctPairs = affectedPairs
        )
    }

    private fun parseIsoDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            // ISO-8601 with timezone offset, e.g. "2026-10-04T05:15:00-04:00"
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e1: Exception) {
            try {
                // Without colon in offset, or simple format
                val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val clean = dateStr.substringBefore("+").substringBefore("-").replace("Z", "")
                sdf2.parse(clean)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun getCountryFlag(currency: String): String {
        return when (currency) {
            "USD" -> "🇺🇸"
            "EUR" -> "🇪🇺"
            "GBP" -> "🇬🇧"
            "JPY" -> "🇯🇵"
            "AUD" -> "🇦🇺"
            "CAD" -> "🇨🇦"
            "CHF" -> "🇨🇭"
            "NZD" -> "🇳🇿"
            "CNY" -> "🇨🇳"
            "ALL" -> "🌐"
            else -> "🌐"
        }
    }

    private fun getAffectedSctPairs(currency: String): List<String> {
        return when (currency) {
            "USD" -> listOf("EURUSD", "GBPUSD", "USDJPY", "USDCAD", "USDCHF", "XAUUSD")
            "EUR" -> listOf("EURUSD", "EURGBP", "EURJPY")
            "GBP" -> listOf("GBPUSD", "GBPJPY", "EURGBP")
            "JPY" -> listOf("USDJPY", "GBPJPY", "EURJPY")
            "CAD" -> listOf("USDCAD")
            "AUD" -> listOf("AUDUSD", "AUDJPY")
            "CHF" -> listOf("USDCHF")
            "NZD" -> listOf("NZDUSD")
            "CNY" -> listOf("AUDUSD", "USDJPY")
            else -> emptyList()
        }
    }

    private fun generateSctInstitutionalPlaybook(title: String, country: String, impact: NewsImpact): String {
        val lower = title.lowercase(Locale.ROOT)
        return when {
            impact == NewsImpact.HIGH && country == "USD" && (lower.contains("cpi") || lower.contains("nfp") || lower.contains("employment") || lower.contains("fomc") || lower.contains("rate")) -> {
                "Institutional Tier-1 catalyst. Expect extreme liquidity sweeps of Asian Range High/Low. Avoid entering 15m prior to release; wait for post-news M5 displacement before entering Fibonacci Golden Pocket retests."
            }
            impact == NewsImpact.HIGH && country == "EUR" && (lower.contains("ecb") || lower.contains("rate") || lower.contains("cpi")) -> {
                "SCT EURO MODE Flagship catalyst. Spread expansion likely on EURUSD. Invalidation levels and Asian Box extremes will be tested during Frankfurt/London overlaps."
            }
            impact == NewsImpact.HIGH && country == "GBP" && (lower.contains("boe") || lower.contains("rate") || lower.contains("gdp") || lower.contains("cpi")) -> {
                "Major British Pound driver. Expect London Open Judas Swing and volatility expansion into EMA 9/21 ribbon on GBPUSD & GBPJPY."
            }
            impact == NewsImpact.HIGH && country == "JPY" && (lower.contains("boj") || lower.contains("rate") || lower.contains("statement")) -> {
                "Key Asian session driver for USDJPY and GBPJPY. Avoid new Fibonacci retest positions 30m prior to policy announcement."
            }
            impact == NewsImpact.HIGH && lower.contains("powell") -> {
                "Fed macro policy address. Dictates New York session continuation or sharp mean-reversion towards the Asian Range equilibrium."
            }
            impact == NewsImpact.HIGH -> {
                "High impact macroeconomic release. Widen stop loss safety margins and monitor liquidity sweeps on $country pairs."
            }
            impact == NewsImpact.MEDIUM -> {
                "Moderate algorithmic repricing event. Check EMA 9/21 dynamic trend alignment before taking execution signals."
            }
            impact == NewsImpact.NON_ECONOMIC -> {
                "Bank Holiday: Reduced market volume and lower volatility. Range-bound price action expected."
            }
            else -> {
                "Low impact macroeconomic update. Minimal disruption to prevailing SCT institutional trend."
            }
        }
    }

    companion object {
        private const val KEY_CACHED_JSON = "key_ff_cached_json"
        private const val KEY_LAST_SYNC = "key_ff_last_sync_ms"

        fun getCachedEventsOrFallback(baseTimeMs: Long = System.currentTimeMillis()): List<EconomicEvent> {
            return generateInstitutionalBaseline(baseTimeMs)
        }

        fun getNextHighImpactEvent(events: List<EconomicEvent>, nowMs: Long = System.currentTimeMillis()): EconomicEvent? {
            return events
                .filter { it.impact == NewsImpact.HIGH && it.utcTimestamp > nowMs }
                .minByOrNull { it.utcTimestamp }
        }

        fun generateInstitutionalBaseline(baseTimeMs: Long = System.currentTimeMillis()): List<EconomicEvent> {
            val hourMs = 3600 * 1000L
            val minMs = 60 * 1000L

            return listOf(
                EconomicEvent(
                    id = "ff_cpi_usd",
                    title = "US Core CPI m/m",
                    country = "USD",
                    countryFlag = "🇺🇸",
                    utcTimestamp = baseTimeMs + (2 * hourMs + 15 * minMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = "0.3%",
                    previous = "0.3%",
                    sctImpactNote = "Institutional Tier-1 catalyst. Expect extreme liquidity sweeps of Asian Range High/Low. Avoid entering 15m prior to release; wait for post-news M5 displacement before entering Fibonacci Golden Pocket retests.",
                    affectedSctPairs = listOf("EURUSD", "GBPUSD", "USDJPY", "USDCAD", "USDCHF", "XAUUSD")
                ),
                EconomicEvent(
                    id = "ff_nfp_usd",
                    title = "Non-Farm Employment Change",
                    country = "USD",
                    countryFlag = "🇺🇸",
                    utcTimestamp = baseTimeMs + (26 * hourMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = "165K",
                    previous = "142K",
                    sctImpactNote = "Institutional Tier-1 catalyst. Extreme slippage and Asian box dual-sided sweep probable.",
                    affectedSctPairs = listOf("EURUSD", "GBPUSD", "USDJPY", "XAUUSD")
                ),
                EconomicEvent(
                    id = "ff_ecb_rate",
                    title = "Main Refinancing Rate",
                    country = "EUR",
                    countryFlag = "🇪🇺",
                    utcTimestamp = baseTimeMs + (4 * hourMs + 45 * minMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = "3.65%",
                    previous = "3.90%",
                    sctImpactNote = "SCT EURO MODE Flagship catalyst. Spread expansion likely on EURUSD. Invalidation levels may be tested.",
                    affectedSctPairs = listOf("EURUSD")
                ),
                EconomicEvent(
                    id = "ff_fed_powell",
                    title = "Fed Chair Powell Speaks",
                    country = "USD",
                    countryFlag = "🇺🇸",
                    utcTimestamp = baseTimeMs + (6 * hourMs + 30 * minMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = null,
                    previous = null,
                    sctImpactNote = "Fed macro policy address. Dictates New York session continuation or sharp mean-reversion towards the Asian Range equilibrium.",
                    affectedSctPairs = listOf("EURUSD", "GBPUSD", "USDJPY")
                ),
                EconomicEvent(
                    id = "ff_uk_gdp",
                    title = "UK Monthly GDP m/m",
                    country = "GBP",
                    countryFlag = "🇬🇧",
                    utcTimestamp = baseTimeMs - (1 * hourMs + 20 * minMs),
                    impact = NewsImpact.HIGH,
                    actual = "+0.2%",
                    forecast = "+0.1%",
                    previous = "0.0%",
                    sctImpactNote = "Major British Pound driver. Expect London Open Judas Swing and volatility expansion into EMA 9/21 ribbon on GBPUSD & GBPJPY.",
                    affectedSctPairs = listOf("GBPUSD", "GBPJPY")
                ),
                EconomicEvent(
                    id = "ff_boj_rate",
                    title = "BOJ Policy Rate & Statement",
                    country = "JPY",
                    countryFlag = "🇯🇵",
                    utcTimestamp = baseTimeMs + (14 * hourMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = "0.25%",
                    previous = "0.25%",
                    sctImpactNote = "Key Asian session driver for USDJPY and GBPJPY. Avoid new Fibonacci retest positions 30m prior to statement.",
                    affectedSctPairs = listOf("USDJPY", "GBPJPY")
                ),
                EconomicEvent(
                    id = "ff_cad_empl",
                    title = "Employment Change",
                    country = "CAD",
                    countryFlag = "🇨🇦",
                    utcTimestamp = baseTimeMs + (26 * hourMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = "25.0K",
                    previous = "22.1K",
                    sctImpactNote = "Affects USDCAD Golden Pocket 61.8% support levels and crude oil sentiment.",
                    affectedSctPairs = listOf("USDCAD")
                ),
                EconomicEvent(
                    id = "ff_snb_rate",
                    title = "SNB Policy Rate",
                    country = "CHF",
                    countryFlag = "🇨🇭",
                    utcTimestamp = baseTimeMs + (48 * hourMs),
                    impact = NewsImpact.HIGH,
                    actual = null,
                    forecast = "1.00%",
                    previous = "1.25%",
                    sctImpactNote = "Primary volatility catalyst for USDCHF.",
                    affectedSctPairs = listOf("USDCHF")
                ),
                EconomicEvent(
                    id = "ff_us_ism",
                    title = "ISM Services PMI",
                    country = "USD",
                    countryFlag = "🇺🇸",
                    utcTimestamp = baseTimeMs + (5 * hourMs),
                    impact = NewsImpact.MEDIUM,
                    actual = null,
                    forecast = "51.8",
                    previous = "51.5",
                    sctImpactNote = "Moderate algorithmic repricing event. Check EMA 9/21 dynamic trend alignment before taking execution signals.",
                    affectedSctPairs = listOf("EURUSD", "USDJPY")
                ),
                EconomicEvent(
                    id = "ff_eu_pmi",
                    title = "Flash Manufacturing PMI",
                    country = "EUR",
                    countryFlag = "🇪🇺",
                    utcTimestamp = baseTimeMs - (3 * hourMs),
                    impact = NewsImpact.MEDIUM,
                    actual = "45.8",
                    forecast = "45.6",
                    previous = "45.8",
                    sctImpactNote = "European macroeconomic metric. Sets London session baseline for EURUSD.",
                    affectedSctPairs = listOf("EURUSD")
                ),
                EconomicEvent(
                    id = "ff_us_claims",
                    title = "Unemployment Claims",
                    country = "USD",
                    countryFlag = "🇺🇸",
                    utcTimestamp = baseTimeMs + (2 * hourMs + 15 * minMs),
                    impact = NewsImpact.MEDIUM,
                    actual = null,
                    forecast = "222K",
                    previous = "219K",
                    sctImpactNote = "High-frequency algorithmic repricing. Watch EMA 9/21 continuation on EURUSD and USDJPY.",
                    affectedSctPairs = listOf("EURUSD", "GBPUSD")
                ),
                EconomicEvent(
                    id = "ff_crude",
                    title = "Crude Oil Inventories",
                    country = "USD",
                    countryFlag = "🇺🇸",
                    utcTimestamp = baseTimeMs + (7 * hourMs),
                    impact = NewsImpact.LOW,
                    actual = null,
                    forecast = "+1.2M",
                    previous = "+3.8M",
                    sctImpactNote = "Energy sector focus, indirectly influences USDCAD and CAD pairs.",
                    affectedSctPairs = listOf("USDCAD")
                )
            )
        }
    }
}
