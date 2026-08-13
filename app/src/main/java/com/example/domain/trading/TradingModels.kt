package com.example.domain.trading

data class Candle(
    val timestamp: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double? = null
)

data class SwingPoint(
    val index: Int,
    val price: Double
)

data class SwingResult(
    val highs: List<SwingPoint>,
    val lows: List<SwingPoint>
)

data class TradeLevels(
    val entry: Double,
    val sl: Double,
    val tp1: Double,
    val tp2: Double,
    val tp3: Double,
    val rr: Double
)

data class ConfirmationDetails(
    val higherTimeframeTrend: Boolean,
    val keyLevelConfirmed: Boolean,
    val liquiditySweep: Boolean,
    val bosChochDetected: Boolean,
    val retestConfirmed: Boolean,
    val momentumAligned: Boolean,
    val riskRewardValid: Boolean
)

data class SmcConfluenceBreakdown(
    val correctMarketStructure: Boolean, // +1 pt (HH+HL / LH+LL)
    val liquiditySweep: Boolean,          // +2 pts (Sweep of previous/equal high/low)
    val mssChochDetected: Boolean,        // +2 pts (Market Structure Shift / CHoCH)
    val bosDetected: Boolean,             // +1 pt (Break of Structure)
    val displacement: Boolean,           // +1 pt (Strong momentum candle)
    val orderBlockDetected: Boolean,      // +1 pt (Order Block retest)
    val fvgDetected: Boolean,             // +1 pt (Fair Value Gap retest)
    val premiumDiscountValid: Boolean,   // +1 pt (Discount for Buy, Premium for Sell)
    val totalScore: Int,                 // Max 10 pts
    val bias: String,                     // "BUY", "SELL", "NEUTRAL"
    val activeStep: String,              // "LIQUIDITY_SWEEP", "MSS_CHOCH", "DISPLACEMENT", "BOS", "OB_FVG_RETEST", "PREMIUM_DISCOUNT", "ENTRY_READY"
    val orderBlockRange: Pair<Double, Double>? = null,
    val fvgRange: Pair<Double, Double>? = null,
    val invalidationSl: Double = 0.0,
    val targetLiquidityTp: Double = 0.0
)

data class FullStructureAnalysis(
    val confluence: SmcConfluenceBreakdown,
    val executionRecommendation: String, // "HIGH CONFLUENCE ENTRY", "WAIT FOR RETEST", "INVALID / NO SETUP"
    val summaryText: String
)

data class SmaSignalResult(
    val signal: String, // "BUY", "SELL", "NEUTRAL"
    val smaFast: Double,
    val smaSlow: Double,
    val fastPeriod: Int = 9,
    val slowPeriod: Int = 21,
    val crossoverType: String, // "GOLDEN_CROSS", "DEATH_CROSS", "BULLISH_TREND", "BEARISH_TREND", "NEUTRAL"
    val smaFastSeries: List<Double?> = emptyList(),
    val smaSlowSeries: List<Double?> = emptyList(),
    val signalIndices: List<Pair<Int, String>> = emptyList() // candle index to signal type
)

data class AnalysisResult(
    val direction: String, // "BUY", "SELL", "NO TRADE"
    val score: Int,
    val details: ConfirmationDetails,
    val levels: TradeLevels?,
    val trend: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val smaSignal: SmaSignalResult? = null,
    val fullStructure: FullStructureAnalysis? = null
)
