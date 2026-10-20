package com.example.data.model

data class DaySetupPerformance(
    val dayNumber: Int, // 1 to 5 (1 = most recent / yesterday, 5 = 5 days ago)
    val dateString: String, // e.g. "Friday, Oct 10"
    val dayOfWeek: String, // "FRI", "THU", "WED", "TUE", "MON"
    val fullDate: String, // "Oct 10, 2026"
    val bias: String, // "BULLISH BREAKOUT", "BEARISH BREAKOUT"
    val isLong: Boolean,
    val setupStrategy: String, // "Asian Range Retest + PDH Expansion", "EMA 9/21 Pocket Bounce", "0.618 Fib Retracement"
    val sessionRangeHigh: Double,
    val sessionRangeLow: Double,
    val pdh: Double, // Previous Day High
    val pdl: Double, // Previous Day Low
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitPrice: Double,
    val isWin: Boolean,
    val status: String, // "TP REACHED (WIN)", "SL REACHED (LOSS)"
    val pips: Double, // signed pips (positive for win, negative for loss)
    val triggerTime: String, // "09:35 UTC"
    val exitTime: String, // "13:15 UTC"
    val riskRewardRatio: Double = 1.5,
    val notes: String
)

data class PairFiveDayPerformance(
    val symbol: String,
    val name: String,
    val assetClass: AssetClass,
    val currentPrice: Double,
    val pipSize: Double,
    val mode: String, // "EURO MODE (M5 Asian Range & PDH/PDL)", "USD MODE (EMA 9/21 & 0.618 Fib)", "INSTITUTIONAL RANGE"
    val dailySetups: List<DaySetupPerformance>,
    val totalWins: Int,
    val totalLosses: Int,
    val winRatePercent: Double,
    val totalPipsGained: Double,
    val totalPipsLost: Double,
    val netPips: Double,
    val avgPipsPerDay: Double,
    val profitFactor: Double,
    val bestDayPips: Double,
    val worstDayPips: Double
)
