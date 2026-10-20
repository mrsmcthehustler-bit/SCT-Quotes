package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.NetworkClient
import com.example.data.model.AssetClass
import com.example.data.model.BiquoteTick
import com.example.data.model.EnumSctMode
import com.example.data.model.MarketMover
import com.example.data.model.MarketSummary
import com.example.data.model.OhlcCandle
import com.example.data.model.RiskCalcResult
import com.example.data.model.SctSettings
import com.example.data.model.SupportedTimeZones
import com.example.data.model.TimeZoneSetting
import com.example.data.model.TradingSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

data class InstrumentInfo(
    val symbol: String,
    val name: String,
    val assetClass: AssetClass,
    val defaultPrice: Double,
    val defaultSpread: Double
)

data class ApiDiagnostics(
    val isLive: Boolean = false,
    val latencyMs: Long = 0L,
    val lastSyncTime: Long = 0L,
    val endpointUrl: String = "https://biquote.io/api",
    val statusMessage: String = "Connecting to Biquote API…",
    val successfulCalls: Int = 0,
    val failedCalls: Int = 0
)

class BiquoteRepository(private val context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("sct_quotes_prefs", Context.MODE_PRIVATE)

    private val apiService = NetworkClient.apiService

    // Registry of supported instruments across asset classes, highlighting SCT Theory pairs
    val supportedInstruments = listOf(
        InstrumentInfo("EURUSD", "Euro / US Dollar", AssetClass.FOREX, 1.0845, 0.00012),
        InstrumentInfo("GBPUSD", "British Pound / USD", AssetClass.FOREX, 1.3020, 0.00018),
        InstrumentInfo("GBPJPY", "British Pound / Japanese Yen", AssetClass.FOREX, 194.50, 0.022),
        InstrumentInfo("USDJPY", "US Dollar / Japanese Yen", AssetClass.FOREX, 149.25, 0.015),
        InstrumentInfo("USDCHF", "US Dollar / Swiss Franc", AssetClass.FOREX, 0.8620, 0.00014),
        InstrumentInfo("USDCAD", "US Dollar / Canadian Dollar", AssetClass.FOREX, 1.3780, 0.00016),
        InstrumentInfo("AUDUSD", "Australian Dollar / USD", AssetClass.FOREX, 0.6710, 0.00014),
        InstrumentInfo("BTCUSD", "Bitcoin / USD", AssetClass.CRYPTO, 64280.0, 1.20),
        InstrumentInfo("ETHUSD", "Ethereum / USD", AssetClass.CRYPTO, 2650.0, 0.45),
        InstrumentInfo("SOLUSD", "Solana / USD", AssetClass.CRYPTO, 152.4, 0.08),
        InstrumentInfo("XRPUSD", "Ripple / USD", AssetClass.CRYPTO, 0.584, 0.0003),
        InstrumentInfo("XAUUSD", "Gold Spot / US Dollar", AssetClass.METALS, 2655.50, 0.28),
        InstrumentInfo("XAGUSD", "Silver Spot / US Dollar", AssetClass.METALS, 31.85, 0.02),
        InstrumentInfo("SPX500", "S&P 500 Index CFD", AssetClass.INDICES, 5740.0, 0.50),
        InstrumentInfo("NAS100", "US Tech 100 Index CFD", AssetClass.INDICES, 20120.0, 1.00)
    )

    private val _sctSettings = MutableStateFlow(loadSctSettings())
    val sctSettings: StateFlow<SctSettings> = _sctSettings.asStateFlow()

    private val _ticks = MutableStateFlow<Map<String, BiquoteTick>>(emptyMap())
    val ticks: StateFlow<Map<String, BiquoteTick>> = _ticks.asStateFlow()

    private val _signals = MutableStateFlow<List<TradingSignal>>(emptyList())
    val signals: StateFlow<List<TradingSignal>> = _signals.asStateFlow()

    private val _gainers = MutableStateFlow<List<MarketMover>>(emptyList())
    val gainers: StateFlow<List<MarketMover>> = _gainers.asStateFlow()

    private val _losers = MutableStateFlow<List<MarketMover>>(emptyList())
    val losers: StateFlow<List<MarketMover>> = _losers.asStateFlow()

    private val _mostActive = MutableStateFlow<List<MarketMover>>(emptyList())
    val mostActive: StateFlow<List<MarketMover>> = _mostActive.asStateFlow()

    private val _marketSummary = MutableStateFlow(MarketSummary())
    val marketSummary: StateFlow<MarketSummary> = _marketSummary.asStateFlow()

    private val _apiDiagnostics = MutableStateFlow(ApiDiagnostics())
    val apiDiagnostics: StateFlow<ApiDiagnostics> = _apiDiagnostics.asStateFlow()

    private val bookmarkedSymbols = mutableSetOf<String>()

    init {
        loadBookmarkedSymbols()
        seedInitialData()
    }

    private fun loadSctSettings(): SctSettings {
        val modeStr = sharedPrefs.getString("sct_mode", EnumSctMode.MODE_EURO.name) ?: EnumSctMode.MODE_EURO.name
        val mode = try { EnumSctMode.valueOf(modeStr) } catch (_: Exception) { EnumSctMode.MODE_EURO }
        val fastEma = sharedPrefs.getInt("sct_fast_ema", 9)
        val slowEma = sharedPrefs.getInt("sct_slow_ema", 21)
        val rr = sharedPrefs.getFloat("sct_rr_ratio", 1.5f).toDouble()
        val fibo = sharedPrefs.getFloat("sct_fibo_level", 0.618f).toDouble()
        val asianStart = sharedPrefs.getInt("sct_asian_start", 0)
        val asianEnd = sharedPrefs.getInt("sct_asian_end", 6)
        val baseUtc = sharedPrefs.getInt("sct_base_utc", 9)
        val gmtOffset = sharedPrefs.getInt("sct_gmt_offset", 2)
        val autoGmt = sharedPrefs.getBoolean("sct_auto_gmt", true)
        val watermark = sharedPrefs.getBoolean("sct_show_watermark", true)

        val ninjaEnabled = sharedPrefs.getBoolean("sct_ninja_enabled", false)
        val ninjaBreakouts = sharedPrefs.getBoolean("sct_ninja_breakouts", false)
        val ninjaRisk = sharedPrefs.getFloat("sct_ninja_risk", 0.5f).toDouble()
        val ninjaTarget = sharedPrefs.getFloat("sct_ninja_target", 1.5f).toDouble()
        val ninjaSLBuffer = sharedPrefs.getInt("sct_ninja_sl_buffer", 20)
        val ninjaCloses = sharedPrefs.getInt("sct_ninja_closes", 2)
        val ninjaLookback = sharedPrefs.getInt("sct_ninja_lookback", 10)
        val ninjaTPHunter = sharedPrefs.getBoolean("sct_ninja_tp_hunter", true)
        val ninjaOneTrade = sharedPrefs.getBoolean("sct_ninja_one_trade", true)
        val ninjaPartialTP = sharedPrefs.getBoolean("sct_ninja_partial_tp", true)
        val ninjaPartialTPR = sharedPrefs.getFloat("sct_ninja_partial_r", 1.5f).toDouble()
        val ninjaMoveBE = sharedPrefs.getBoolean("sct_ninja_move_be", true)
        val ninjaPyramiding = sharedPrefs.getBoolean("sct_ninja_pyramiding", true)
        val ninjaMagic = sharedPrefs.getLong("sct_ninja_magic", 888999L)

        val ninja = com.example.data.model.NinjaSettings(
            enabled = ninjaEnabled,
            tradeZoneBreakouts = ninjaBreakouts,
            riskPercent = ninjaRisk,
            targetPercent = ninjaTarget,
            slBufferPoints = ninjaSLBuffer,
            requiredCloses = ninjaCloses,
            swingLookback = ninjaLookback,
            enableTPHunter = ninjaTPHunter,
            oneTradePerDay = ninjaOneTrade,
            enablePartialTP = ninjaPartialTP,
            partialTPR = ninjaPartialTPR,
            moveToBreakEven = ninjaMoveBE,
            enablePyramiding = ninjaPyramiding,
            magicNumber = ninjaMagic
        )

        return SctSettings(
            operationalMode = mode,
            fastEmaPeriod = fastEma,
            slowEmaPeriod = slowEma,
            riskRewardRatio = rr,
            fiboRetracementLevel = fibo,
            asianStartHour = asianStart,
            asianEndHour = asianEnd,
            baseUtcStart = baseUtc,
            manualGmtOffset = gmtOffset,
            autoDetectGmt = autoGmt,
            showWatermark = watermark,
            ninjaSettings = ninja
        )
    }

    fun updateSctSettings(newSettings: SctSettings) {
        _sctSettings.value = newSettings
        sharedPrefs.edit()
            .putString("sct_mode", newSettings.operationalMode.name)
            .putInt("sct_fast_ema", newSettings.fastEmaPeriod)
            .putInt("sct_slow_ema", newSettings.slowEmaPeriod)
            .putFloat("sct_rr_ratio", newSettings.riskRewardRatio.toFloat())
            .putFloat("sct_fibo_level", newSettings.fiboRetracementLevel.toFloat())
            .putInt("sct_asian_start", newSettings.asianStartHour)
            .putInt("sct_asian_end", newSettings.asianEndHour)
            .putInt("sct_base_utc", newSettings.baseUtcStart)
            .putInt("sct_gmt_offset", newSettings.manualGmtOffset)
            .putBoolean("sct_auto_gmt", newSettings.autoDetectGmt)
            .putBoolean("sct_show_watermark", newSettings.showWatermark)
            .putBoolean("sct_ninja_enabled", newSettings.ninjaSettings.enabled)
            .putBoolean("sct_ninja_breakouts", newSettings.ninjaSettings.tradeZoneBreakouts)
            .putFloat("sct_ninja_risk", newSettings.ninjaSettings.riskPercent.toFloat())
            .putFloat("sct_ninja_target", newSettings.ninjaSettings.targetPercent.toFloat())
            .putInt("sct_ninja_sl_buffer", newSettings.ninjaSettings.slBufferPoints)
            .putInt("sct_ninja_closes", newSettings.ninjaSettings.requiredCloses)
            .putInt("sct_ninja_lookback", newSettings.ninjaSettings.swingLookback)
            .putBoolean("sct_ninja_tp_hunter", newSettings.ninjaSettings.enableTPHunter)
            .putBoolean("sct_ninja_one_trade", newSettings.ninjaSettings.oneTradePerDay)
            .putBoolean("sct_ninja_partial_tp", newSettings.ninjaSettings.enablePartialTP)
            .putFloat("sct_ninja_partial_r", newSettings.ninjaSettings.partialTPR.toFloat())
            .putBoolean("sct_ninja_move_be", newSettings.ninjaSettings.moveToBreakEven)
            .putBoolean("sct_ninja_pyramiding", newSettings.ninjaSettings.enablePyramiding)
            .putLong("sct_ninja_magic", newSettings.ninjaSettings.magicNumber)
            .apply()

        recalculateAllSignals()
    }

    fun resetSctSettings() {
        val defaults = SctSettings()
        updateSctSettings(defaults)
    }

    private fun recalculateAllSignals() {
        val currentTicks = _ticks.value
        val settings = _sctSettings.value

        _signals.value = _signals.value.map { signal ->
            val tick = currentTicks[signal.symbol]
            val latestPrice = tick?.currentPrice ?: signal.currentPrice
            val spread = tick?.calculatedSpread ?: signal.spread

            TradingSignalEngine.evaluateSctSignal(
                symbol = signal.symbol,
                name = signal.name,
                assetClass = signal.assetClass,
                currentPrice = latestPrice,
                spread = spread,
                candles = signal.candles,
                settings = settings
            ).copy(isBookmarked = bookmarkedSymbols.contains(signal.symbol))
        }
    }

    private fun loadBookmarkedSymbols() {
        val saved = sharedPrefs.getStringSet("bookmarked_symbols", null)
        if (saved != null) {
            bookmarkedSymbols.addAll(saved)
        } else {
            // Default favorites
            bookmarkedSymbols.addAll(listOf("EURUSD", "GBPUSD", "USDJPY", "XAUUSD"))
        }
    }

    fun toggleBookmark(symbol: String): Boolean {
        val isNowBookmarked: Boolean
        if (bookmarkedSymbols.contains(symbol)) {
            bookmarkedSymbols.remove(symbol)
            isNowBookmarked = false
        } else {
            bookmarkedSymbols.add(symbol)
            isNowBookmarked = true
        }
        sharedPrefs.edit().putStringSet("bookmarked_symbols", HashSet(bookmarkedSymbols)).apply()

        _signals.value = _signals.value.map {
            if (it.symbol == symbol) it.copy(isBookmarked = isNowBookmarked) else it
        }
        return isNowBookmarked
    }

    fun isBookmarked(symbol: String): Boolean = bookmarkedSymbols.contains(symbol)

    private fun seedInitialData() {
        val initialTickMap = mutableMapOf<String, BiquoteTick>()
        val initialSignals = mutableListOf<TradingSignal>()
        val settings = _sctSettings.value

        supportedInstruments.forEach { info ->
            val tick = BiquoteTick(
                symbol = info.symbol,
                bid = info.defaultPrice - (info.defaultSpread / 2.0),
                ask = info.defaultPrice + (info.defaultSpread / 2.0),
                mid = info.defaultPrice,
                spread = info.defaultSpread,
                direction = 1,
                change = (Math.random() * 2.0 - 0.9) * (info.defaultPrice * 0.005),
                changePercent = (Math.random() * 2.8 - 1.2),
                high = info.defaultPrice * 1.008,
                low = info.defaultPrice * 0.992
            )
            initialTickMap[info.symbol] = tick

            val candles = TradingSignalEngine.generateSyntheticCandles(
                currentPrice = info.defaultPrice,
                volatility = if (info.assetClass == AssetClass.CRYPTO) 0.008 else 0.0025,
                count = 35
            )

            val signal = TradingSignalEngine.evaluateSctSignal(
                symbol = info.symbol,
                name = info.name,
                assetClass = info.assetClass,
                currentPrice = info.defaultPrice,
                spread = info.defaultSpread,
                candles = candles,
                settings = settings
            ).copy(isBookmarked = bookmarkedSymbols.contains(info.symbol))

            initialSignals.add(signal)
        }

        _ticks.value = initialTickMap
        _signals.value = initialSignals
        generateInitialMovers(initialTickMap)
    }

    private fun generateInitialMovers(ticks: Map<String, BiquoteTick>) {
        val moversList = supportedInstruments.mapNotNull { info ->
            val t = ticks[info.symbol] ?: return@mapNotNull null
            MarketMover(
                symbol = info.symbol,
                price = t.currentPrice,
                changePercent = t.changePercent ?: 0.8,
                name = info.name,
                assetClass = info.assetClass,
                volumeUsd = (info.defaultPrice * (5000 + Math.random() * 20000))
            )
        }

        _gainers.value = moversList.sortedByDescending { it.changePercent }.take(6)
        _losers.value = moversList.sortedBy { it.changePercent }.take(6)
        _mostActive.value = moversList.sortedByDescending { it.volumeUsd }.take(6)
    }

    /**
     * Refreshes market data by calling Biquote API endpoints with fallback handling.
     */
    suspend fun refreshData() = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var anySuccess = false
        val updatedTicks = _ticks.value.toMutableMap()
        var succCount = _apiDiagnostics.value.successfulCalls
        var failCount = _apiDiagnostics.value.failedCalls

        // Priority SCT symbols to fetch live from Biquote API
        val prioritySymbols = listOf("EURUSD", "GBPUSD", "USDJPY", "USDCHF", "USDCAD", "XAUUSD")

        for (sym in prioritySymbols) {
            try {
                val response = apiService.getTick(sym)
                if (response.isSuccessful && response.body() != null) {
                    val tick = response.body()!!
                    updatedTicks[sym] = tick
                    anySuccess = true
                    succCount++
                } else {
                    failCount++
                }
            } catch (e: Exception) {
                failCount++
            }
        }

        // Fetch market gainers / losers if available
        try {
            val gainersResponse = apiService.getMarketGainers()
            if (gainersResponse.isSuccessful && !gainersResponse.body().isNullOrEmpty()) {
                val list = gainersResponse.body()!!.map { tick ->
                    MarketMover(
                        symbol = tick.symbol,
                        price = tick.currentPrice,
                        changePercent = tick.changePercent ?: 0.0,
                        name = supportedInstruments.find { it.symbol == tick.symbol }?.name ?: tick.symbol,
                        assetClass = supportedInstruments.find { it.symbol == tick.symbol }?.assetClass ?: AssetClass.FOREX
                    )
                }
                _gainers.value = list
                anySuccess = true
            }
        } catch (_: Exception) {}

        // For symbols without direct tick, simulate realistic micro-movements
        supportedInstruments.forEach { info ->
            val existing = updatedTicks[info.symbol]
            if (existing == null || !prioritySymbols.contains(info.symbol)) {
                val curPrice = existing?.currentPrice ?: info.defaultPrice
                val delta = (Math.random() - 0.495) * 0.0006 * curPrice
                val newPrice = (curPrice + delta).coerceAtLeast(0.0001)
                val spread = existing?.calculatedSpread ?: info.defaultSpread

                updatedTicks[info.symbol] = BiquoteTick(
                    symbol = info.symbol,
                    bid = newPrice - (spread / 2.0),
                    ask = newPrice + (spread / 2.0),
                    mid = newPrice,
                    spread = spread,
                    direction = if (delta >= 0) 1 else -1,
                    change = (existing?.change ?: 0.0) + delta,
                    changePercent = (existing?.changePercent ?: 0.0) + (delta / newPrice * 100),
                    high = maxOf(existing?.high ?: newPrice, newPrice),
                    low = minOf(existing?.low ?: newPrice, newPrice)
                )
            }
        }

        _ticks.value = updatedTicks

        // Re-evaluate signals with updated quotes and SCT Theory
        val settings = _sctSettings.value
        val newSignals = _signals.value.map { signal ->
            val tick = updatedTicks[signal.symbol]
            val latestPrice = tick?.currentPrice ?: signal.currentPrice
            val spread = tick?.calculatedSpread ?: signal.spread

            val updatedCandles = signal.candles.toMutableList()
            if (updatedCandles.isNotEmpty()) {
                val last = updatedCandles.last()
                updatedCandles[updatedCandles.lastIndex] = last.copy(
                    close = latestPrice,
                    high = maxOf(last.high, latestPrice),
                    low = minOf(last.low, latestPrice)
                )
            }

            TradingSignalEngine.evaluateSctSignal(
                symbol = signal.symbol,
                name = signal.name,
                assetClass = signal.assetClass,
                currentPrice = latestPrice,
                spread = spread,
                candles = updatedCandles,
                settings = settings
            ).copy(isBookmarked = bookmarkedSymbols.contains(signal.symbol))
        }
        _signals.value = newSignals

        val latency = System.currentTimeMillis() - startTime
        _apiDiagnostics.value = ApiDiagnostics(
            isLive = anySuccess,
            latencyMs = latency,
            lastSyncTime = System.currentTimeMillis(),
            endpointUrl = "https://biquote.io/api",
            statusMessage = if (anySuccess) "Live Biquote API Feed Connected ($latency ms)" else "Standby Mode: SCT Indicator Active",
            successfulCalls = succCount,
            failedCalls = failCount
        )

        generateInitialMovers(updatedTicks)
    }

    suspend fun getCandlesForSymbol(symbol: String, timeframe: String): List<OhlcCandle> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOhlc(symbol, timeframe)
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext response.body()!!
            }
        } catch (_: Exception) {}

        val curPrice = _ticks.value[symbol]?.currentPrice ?: 1.0
        val count = when (timeframe) {
            "M5" -> 50
            "M15" -> 40
            "H1" -> 30
            "H4" -> 25
            "D1" -> 20
            else -> 35
        }
        return@withContext TradingSignalEngine.generateSyntheticCandles(curPrice, count = count)
    }

    fun calculateRisk(
        accountBalance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLoss: Double,
        tp1: Double,
        tp2: Double,
        assetClass: AssetClass
    ): RiskCalcResult {
        val safeBalance = accountBalance.coerceAtLeast(10.0)
        val safeRiskPct = riskPercent.coerceIn(0.1, 10.0)
        val riskAmount = safeBalance * (safeRiskPct / 100.0)

        val slDistance = abs(entryPrice - stopLoss).coerceAtLeast(0.00001)
        val tp1Distance = abs(tp1 - entryPrice)
        val tp2Distance = abs(tp2 - entryPrice)

        val lotSize: Double = when (assetClass) {
            AssetClass.FOREX -> {
                val pips = slDistance / 0.0001
                if (pips > 0) ((riskAmount / (pips * 10.0)) * 100.0).roundToInt() / 100.0 else 0.1
            }
            AssetClass.CRYPTO -> ((riskAmount / slDistance) * 1000.0).roundToInt() / 1000.0
            AssetClass.METALS -> ((riskAmount / (slDistance * 100.0)) * 100.0).roundToInt() / 100.0
            AssetClass.INDICES -> ((riskAmount / slDistance) * 10.0).roundToInt() / 10.0
            AssetClass.ALL -> 0.1
        }

        val tp1Profit = if (slDistance > 0) (riskAmount * (tp1Distance / slDistance)) else 0.0
        val tp2Profit = if (slDistance > 0) (riskAmount * (tp2Distance / slDistance)) else 0.0
        val rr = if (slDistance > 0) ((tp1Distance / slDistance) * 10.0).roundToInt() / 10.0 else 1.5

        return RiskCalcResult(
            accountBalance = safeBalance,
            riskPercent = safeRiskPct,
            riskAmountUsd = riskAmount,
            stopLossDistance = slDistance,
            recommendedLotSize = lotSize.coerceAtLeast(0.01),
            targetProfitTp1Usd = tp1Profit,
            targetProfitTp2Usd = tp2Profit,
            riskRewardRatio = rr
        )
    }

    fun loadSavedTimeZone(): TimeZoneSetting {
        val tzId = sharedPrefs.getString("sct_timezone_id", SupportedTimeZones.UTC.id) ?: SupportedTimeZones.UTC.id
        return SupportedTimeZones.ALL_TIME_ZONES.find { it.id == tzId } ?: SupportedTimeZones.UTC
    }

    fun saveTimeZone(timeZoneId: String) {
        sharedPrefs.edit().putString("sct_timezone_id", timeZoneId).apply()
    }

    fun loadSelectedPairs(): Set<String> {
        val saved = sharedPrefs.getStringSet("sct_selected_pairs", null)
        return saved ?: setOf("EURUSD", "GBPUSD", "USDJPY", "USDCAD", "USDCHF", "XAUUSD")
    }

    fun saveSelectedPairs(pairs: Set<String>) {
        sharedPrefs.edit().putStringSet("sct_selected_pairs", pairs).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPrefs.getBoolean("sct_pairs_onboarded", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPrefs.edit().putBoolean("sct_pairs_onboarded", completed).apply()
    }

    fun loadAlertsEnabled(): Boolean {
        return sharedPrefs.getBoolean("sct_alerts_enabled", true)
    }

    fun saveAlertsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("sct_alerts_enabled", enabled).apply()
    }

    fun loadNinjaAlertsEnabled(): Boolean {
        return sharedPrefs.getBoolean("sct_ninja_alerts_enabled", true)
    }

    fun saveNinjaAlertsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("sct_ninja_alerts_enabled", enabled).apply()
    }
}
