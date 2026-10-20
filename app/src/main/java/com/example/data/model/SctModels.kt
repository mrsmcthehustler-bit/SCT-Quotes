package com.example.data.model

enum class EnumSctMode(val title: String, val description: String) {
    MODE_EURO("EURO MODE", "EURUSD M5 Strategy with PDH/PDL & Asian Range"),
    MODE_USD("USD MODE", "USD Pairs (USDJPY, USDCHF, USDCAD, GBPJPY, GBPUSD)")
}

enum class SctStrategyArchetype(
    val title: String,
    val shortLabel: String,
    val description: String,
    val primaryPairs: String
) {
    ALL("All SCT Setups", "ALL", "Complete institutional SCT Theory universe", "All Pairs"),
    EURO_MODE_BREAKOUT("Euro Mode (M5 Breakout/Retest)", "EURO M5", "Asian Range Breakout with Wick Retest targeting PDH/PDL", "EURUSD"),
    USD_MODE_POCKET("USD Mode (EMA 9/21 Pocket)", "EMA POCKET", "EMA 9 & 21 Ribbon crossover and pullback pocket taps", "GBPUSD, GBPJPY"),
    USD_MODE_FIBONACCI("USD Mode (Fibonacci 61.8%)", "FIBO 61.8%", "Session impulse retracement into 50% - 61.8% Golden Pocket", "USDJPY, USDCHF, USDCAD"),
    COMMODITY_LIQUIDITY("Session Liquidity Sweeps", "LIQUIDITY", "Asian session range sweep targeting external daily pools", "XAUUSD, AUDUSD"),
    NINJA_USDJPY("Ninja Mode (USDJPY SC Zone V2.80)", "NINJA M5", "SC Zone 09-11 Breakout with M15 Order Block, TP Hunter & Safety Rules", "USDJPY")
}

data class SctExecutionStep(
    val stepNumber: Int,
    val title: String,
    val status: String, // "COMPLETED", "TRIGGERED", "PENDING", "SECURED"
    val detail: String,
    val isSuccess: Boolean = true
)

data class SctSessionInfo(
    val sessionName: String = "London / Frankfurt Window",
    val gmtTimeString: String = "08:45 GMT",
    val statusDescription: String = "High Liquidity Execution Window Active",
    val isAsianRangeLocked: Boolean = true,
    val isLondonBreakoutWindow: Boolean = true,
    val isNewYorkExpansion: Boolean = false
)

data class NinjaSettings(
    val enabled: Boolean = false,
    val tradeZoneBreakouts: Boolean = false, // Direct execution on SC Zone Break vs M15 OB
    val riskPercent: Double = 0.5,
    val targetPercent: Double = 1.5, // 1:3 Fallback R:R
    val slBufferPoints: Int = 20,
    val requiredCloses: Int = 2, // M5 Closes required for breakout
    val swingLookback: Int = 10,
    val enableTPHunter: Boolean = true,
    val tpFractalLookback: Int = 20,
    val oneTradePerDay: Boolean = true, // Safety Mode (1 Trade Per Day)
    val splitRiskForMulti: Boolean = false,
    val enablePartialTP: Boolean = true,
    val partialTPR: Double = 1.5,
    val partialTPPercent: Double = 50.0,
    val moveToBreakEven: Boolean = true,
    val beBufferPoints: Int = 10,
    val enablePyramiding: Boolean = true,
    val scaleStepR: Double = 1.0,
    val scaleLotMultiplier: Double = 0.5,
    val maxScaleEntries: Int = 3,
    val magicNumber: Long = 888999L
)

data class SctSettings(
    val operationalMode: EnumSctMode = EnumSctMode.MODE_EURO,
    val fastEmaPeriod: Int = 9,
    val slowEmaPeriod: Int = 21,
    val riskRewardRatio: Double = 1.5,
    val fiboRetracementLevel: Double = 0.618,
    val asianStartHour: Int = 0,
    val asianEndHour: Int = 6,
    val autoDetectGmt: Boolean = true,
    val manualGmtOffset: Int = 2,
    val baseUtcStart: Int = 9,
    val daysBack: Int = 20,
    val showWatermark: Boolean = true,
    val watermarkMain: String = "SCT THEORY",
    val watermarkSub: String = "patience | precision | diligence",
    val showDashboard: Boolean = true,
    val ninjaSettings: NinjaSettings = NinjaSettings()
)

data class SctLevels(
    val asianHigh: Double = 0.0,
    val asianLow: Double = 0.0,
    val pdh: Double = 0.0, // Previous Day High
    val pdl: Double = 0.0, // Previous Day Low
    val rangeHigh: Double = 0.0,
    val rangeLow: Double = 0.0,
    val ema9: Double = 0.0,
    val ema21: Double = 0.0,
    val fibEntryLevel: Double? = null,
    val fib50Level: Double? = null,
    val retestLevel: Double? = null,
    val asianCandleCount: Int = 18,
    val ema9Series: List<Double> = emptyList(),
    val ema21Series: List<Double> = emptyList()
)

data class SctDashboardStats(
    val zoneStatus: String = "ACTIVE (EURO MODE M5)",
    val signalBias: String = "NEUTRAL",
    val tradeStatus: String = "NO SIGNAL",
    val entryPrice: Double = 0.0,
    val slPrice: Double = 0.0,
    val tpPrice: Double = 0.0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: Double = 0.0,
    val pipsGained: Double = 0.0,
    val pipsLost: Double = 0.0,
    val netPips: Double = 0.0,
    val avgPipsDay: Double = 0.0,
    val avgPipsWeek: Double = 0.0,
    val avgPipsMonth: Double = 0.0
)

enum class TriggerType(val title: String) {
    EMA_BULLISH_CROSSOVER("EMA Bullish Cross"),
    EMA_BEARISH_CROSSOVER("EMA Bearish Cross"),
    FIB_618_BOUNCE("Fib 61.8% Bounce"),
    FIB_382_RETEST("Fib 38.2% Retest"),
    FIB_50_REJECTION("Fib 50% Rejection"),
    ASIAN_RANGE_BREAKOUT("Asian Range Breakout"),
    WICK_RETEST_CONFIRMED("Wick Retest Confirmed"),
    NINJA_SC_ZONE_BREAKOUT("Ninja SC Zone Breakout"),
    NINJA_M15_ORDER_BLOCK("Ninja M15 Order Block"),
    NINJA_TP_HUNTER_TARGET("Ninja TP Hunter Target"),
    NINJA_SAFETY_RULE("Ninja Safety Mode Active"),
    HARARE_WINDOW_OPEN("Harare Session Open")
}

data class SctTriggerAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val symbol: String,
    val triggerType: TriggerType,
    val timestamp: Long = System.currentTimeMillis(),
    val isBullish: Boolean = true,
    val detail: String = "",
    val isNinjaAlert: Boolean = false
)

fun SctSettings.isHarareSignalWindowActive(timeMs: Long = System.currentTimeMillis()): Boolean {
    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Africa/Harare"))
    cal.timeInMillis = timeMs
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
    return hour in 8..17
}

fun SctSettings.formatHarareTime(timeMs: Long = System.currentTimeMillis()): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss z", java.util.Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("Africa/Harare")
    return sdf.format(java.util.Date(timeMs))
}
