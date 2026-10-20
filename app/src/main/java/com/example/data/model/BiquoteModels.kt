package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class AssetClass(val displayName: String) {
    ALL("All"),
    CRYPTO("Crypto"),
    FOREX("Forex"),
    METALS("Metals"),
    INDICES("Indices")
}

enum class SignalType(val label: String, val isBullish: Boolean, val isBearish: Boolean) {
    STRONG_BUY("STRONG BUY", isBullish = true, isBearish = false),
    BUY("BUY", isBullish = true, isBearish = false),
    NEUTRAL("NEUTRAL", isBullish = false, isBearish = false),
    SELL("SELL", isBullish = false, isBearish = true),
    STRONG_SELL("STRONG SELL", isBullish = false, isBearish = true)
}

@JsonClass(generateAdapter = true)
data class BiquoteTick(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "bid") val bid: Double? = null,
    @Json(name = "ask") val ask: Double? = null,
    @Json(name = "mid") val mid: Double? = null,
    @Json(name = "spread") val spread: Double? = null,
    @Json(name = "direction") val direction: Any? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "change") val change: Double? = null,
    @Json(name = "changePercent") val changePercent: Double? = null,
    @Json(name = "high") val high: Double? = null,
    @Json(name = "low") val low: Double? = null
) {
    val currentPrice: Double
        get() = mid ?: ask ?: bid ?: 0.0

    val calculatedSpread: Double
        get() {
            if (spread != null && spread > 0) return spread
            if (ask != null && bid != null) return (ask - bid).coerceAtLeast(0.0)
            return 0.0
        }
}

@JsonClass(generateAdapter = true)
data class OhlcCandle(
    @Json(name = "open") val open: Double,
    @Json(name = "high") val high: Double,
    @Json(name = "low") val low: Double,
    @Json(name = "close") val close: Double,
    @Json(name = "volume") val volume: Double = 0.0,
    @Json(name = "timestamp") val timestamp: Any? = null
)

@JsonClass(generateAdapter = true)
data class MarketMover(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "changePercent") val changePercent: Double = 0.0,
    val name: String = "",
    val assetClass: AssetClass = AssetClass.CRYPTO,
    val volumeUsd: Double = 0.0
)

data class MarketSummary(
    val gainersCount: Int = 18,
    val losersCount: Int = 12,
    val unchangedCount: Int = 4,
    val marketSentiment: String = "Greed (68)",
    val topSector: String = "Cryptocurrency (+4.2%)"
)

data class TradingSignal(
    val id: String,
    val symbol: String,
    val name: String,
    val assetClass: AssetClass,
    val signalType: SignalType,
    val timeframe: String, // "M15", "H1", "H4", "D1"
    val confidenceScore: Int, // 50 to 98
    val entryPrice: Double,
    val tp1: Double,
    val tp2: Double,
    val stopLoss: Double,
    val riskRewardRatio: Double,
    val currentPrice: Double,
    val spread: Double,
    val rsi: Double,
    val macdHistogram: Double,
    val emaTrend: String,
    val rationale: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val candles: List<OhlcCandle> = emptyList(),
    val sctLevels: SctLevels? = null,
    val sctDashboardStats: SctDashboardStats? = null,
    val sctStrategyType: String = "SCT Institutional",
    val sctArchetype: SctStrategyArchetype = SctStrategyArchetype.ALL,
    val sctLifecycle: String = "TRIGGER_CONFIRMED",
    val executionSteps: List<SctExecutionStep> = emptyList()
) {
    val slPrice: Double get() = stopLoss
    val tpPrice: Double get() = tp1
    val isStandby: Boolean
        get() = signalType == SignalType.NEUTRAL || sctLifecycle.contains("STANDBY") || sctLifecycle.contains("LOCKED")
    val isBullish: Boolean
        get() = signalType.isBullish
    val tradeStatus: String
        get() = sctLifecycle
    val isUsdMode: Boolean
        get() = sctStrategyType.contains("USD", ignoreCase = true) || sctArchetype.name.contains("USD")
    val isHarareTimeWindow: Boolean
        get() = true
    val harareWindowStatus: String
        get() = sctDashboardStats?.zoneStatus ?: "CAT Institutional Window"
    val confidence: Int
        get() = confidenceScore
    val notes: String
        get() = rationale
    val ema9: Double
        get() = sctLevels?.ema9 ?: 0.0
    val ema21: Double
        get() = sctLevels?.ema21 ?: 0.0
    val fib618: Double
        get() = sctLevels?.fibEntryLevel ?: 0.0
    val fib500: Double
        get() = sctLevels?.fib50Level ?: 0.0
    val fib382: Double
        get() = sctLevels?.retestLevel ?: 0.0

    val pnlPercent: Double
        get() {
            if (entryPrice <= 0.0) return 0.0
            val diff = currentPrice - entryPrice
            return if (signalType.isBullish) {
                (diff / entryPrice) * 100.0
            } else if (signalType.isBearish) {
                (-diff / entryPrice) * 100.0
            } else 0.0
        }
}

data class RiskCalcResult(
    val accountBalance: Double,
    val riskPercent: Double,
    val riskAmountUsd: Double,
    val stopLossDistance: Double,
    val recommendedLotSize: Double,
    val targetProfitTp1Usd: Double,
    val targetProfitTp2Usd: Double,
    val riskRewardRatio: Double
)
