package com.example.domain.trading

import kotlin.math.abs

object TradingAnalyzerEngine {

    fun calcEMA(prices: List<Double>, period: Int): Double? {
        if (prices.size < period) return null
        val k = 2.0 / (period + 1)
        var ema = prices[0]
        for (i in 1 until prices.size) {
            ema = prices[i] * k + ema * (1 - k)
        }
        return ema
    }

    fun detectTrend(candles: List<Candle>): String {
        if (candles.size < 20) return "NEUTRAL"
        val closes = candles.map { it.close }
        val ema9 = calcEMA(closes, 9)
        val ema21 = calcEMA(closes, 21)
        if (ema9 == null || ema21 == null) return "NEUTRAL"
        val lastClose = closes.last()
        return when {
            ema9 > ema21 && lastClose > ema21 -> "BULLISH"
            ema9 < ema21 && lastClose < ema21 -> "BEARISH"
            else -> "NEUTRAL"
        }
    }

    fun findSwingHighsLows(candles: List<Candle>, lookback: Int = 3): SwingResult {
        val highs = mutableListOf<SwingPoint>()
        val lows = mutableListOf<SwingPoint>()
        val lb = lookback
        for (i in lb until (candles.size - lb)) {
            var isHigh = true
            var isLow = true
            for (j in (i - lb)..(i + lb)) {
                if (j == i) continue
                if (candles[j].high >= candles[i].high) isHigh = false
                if (candles[j].low <= candles[i].low) isLow = false
            }
            if (isHigh) highs.add(SwingPoint(i, candles[i].high))
            if (isLow) lows.add(SwingPoint(i, candles[i].low))
        }
        return SwingResult(highs, lows)
    }

    fun detectKeyLevel(candles: List<Candle>, currentPrice: Double): Boolean {
        if (candles.size < 10) return false
        val sl = findSwingHighsLows(candles, 3)
        val threshold = currentPrice * 0.005
        for (high in sl.highs) {
            if (abs(high.price - currentPrice) < threshold) return true
        }
        for (low in sl.lows) {
            if (abs(low.price - currentPrice) < threshold) return true
        }
        return false
    }

    fun detectLiquiditySweep(candles: List<Candle>): Boolean {
        if (candles.size < 15) return false
        val recent = candles.takeLast(15)
        val sl = findSwingHighsLows(candles.takeLast(30), 3)
        if (sl.highs.isEmpty() && sl.lows.isEmpty()) return false
        val lastCandle = recent.last()
        val prevHighs = if (sl.highs.size > 1) sl.highs.dropLast(1) else sl.highs
        val prevLows = if (sl.lows.size > 1) sl.lows.dropLast(1) else sl.lows
        for (high in prevHighs) {
            if (lastCandle.high > high.price && lastCandle.close < high.price) return true
        }
        for (low in prevLows) {
            if (lastCandle.low < low.price && lastCandle.close > low.price) return true
        }
        return false
    }

    fun detectBOSCHoCH(candles: List<Candle>, trend: String): Boolean {
        if (candles.size < 20) return false
        val sl = findSwingHighsLows(candles.takeLast(20), 3)
        val lastClose = candles.last().close
        if (trend == "BULLISH" && sl.highs.size >= 2) {
            return lastClose > sl.highs.last().price
        }
        if (trend == "BEARISH" && sl.lows.size >= 2) {
            return lastClose < sl.lows.last().price
        }
        return false
    }

    fun detectRetest(candles: List<Candle>, trend: String): Boolean {
        if (candles.size < 25) return false
        val sl = findSwingHighsLows(candles.takeLast(25), 3)
        val lastCandle = candles.last()
        val threshold = lastCandle.close * 0.008
        if (trend == "BULLISH" && sl.highs.isNotEmpty()) {
            val nearestHigh = sl.highs.last().price
            return abs(lastCandle.low - nearestHigh) < threshold
        }
        if (trend == "BEARISH" && sl.lows.isNotEmpty()) {
            val nearestLow = sl.lows.last().price
            return abs(lastCandle.high - nearestLow) < threshold
        }
        return false
    }

    fun detectMomentum(candles: List<Candle>): Boolean {
        if (candles.size < 14) return false
        val closes = candles.map { it.close }
        val ema9 = calcEMA(closes, 9)
        val ema21 = calcEMA(closes, 21)
        if (ema9 == null || ema21 == null) return false
        val lastClose = closes.last()
        val prevClose = if (closes.size >= 2) closes[closes.size - 2] else lastClose
        val rising = lastClose > prevClose
        return (ema9 > ema21 && rising) || (ema9 < ema21 && !rising)
    }

    fun calcEntrySlTp(candles: List<Candle>, trend: String): TradeLevels? {
        if (candles.size < 5) return null
        val last = candles.last()
        val sl5 = candles.takeLast(5)
        val recentHigh = sl5.maxOf { it.high }
        val recentLow = sl5.minOf { it.low }

        val entry: Double
        val sl: Double
        val risk: Double

        if (trend == "BULLISH") {
            entry = last.close
            sl = recentLow * 0.998
            risk = entry - sl
        } else {
            entry = last.close
            sl = recentHigh * 1.002
            risk = sl - entry
        }

        if (risk <= 0) return null

        val tp1: Double
        val tp2: Double
        val tp3: Double
        val rr: Double

        if (trend == "BULLISH") {
            tp1 = entry + risk
            tp2 = entry + risk * 2
            tp3 = entry + risk * 3
            rr = (tp2 - entry) / (entry - sl)
        } else {
            tp1 = entry - risk
            tp2 = entry - risk * 2
            tp3 = entry - risk * 3
            rr = (entry - tp2) / (sl - entry)
        }

        return TradeLevels(
            entry = entry,
            sl = sl,
            tp1 = tp1,
            tp2 = tp2,
            tp3 = tp3,
            rr = rr
        )
    }

    fun calcSMA(prices: List<Double>, period: Int): List<Double?> {
        val result = ArrayList<Double?>(prices.size)
        if (prices.size < period) {
            repeat(prices.size) { result.add(null) }
            return result
        }
        var sum = 0.0
        for (i in prices.indices) {
            sum += prices[i]
            if (i >= period) {
                sum -= prices[i - period]
            }
            if (i >= period - 1) {
                result.add(sum / period)
            } else {
                result.add(null)
            }
        }
        return result
    }

    fun calculateSmaSignalEngine(candles: List<Candle>, fastPeriod: Int = 9, slowPeriod: Int = 21): SmaSignalResult {
        if (candles.size < slowPeriod) {
            return SmaSignalResult(
                signal = "NEUTRAL",
                smaFast = 0.0,
                smaSlow = 0.0,
                fastPeriod = fastPeriod,
                slowPeriod = slowPeriod,
                crossoverType = "NEUTRAL",
                smaFastSeries = emptyList(),
                smaSlowSeries = emptyList(),
                signalIndices = emptyList()
            )
        }

        val closes = candles.map { it.close }
        val fastSeries = calcSMA(closes, fastPeriod)
        val slowSeries = calcSMA(closes, slowPeriod)

        val signalIndices = mutableListOf<Pair<Int, String>>()

        for (i in slowPeriod until candles.size) {
            val prevFast = fastSeries[i - 1]
            val prevSlow = slowSeries[i - 1]
            val currFast = fastSeries[i]
            val currSlow = slowSeries[i]

            if (prevFast != null && prevSlow != null && currFast != null && currSlow != null) {
                if (prevFast <= prevSlow && currFast > currSlow) {
                    signalIndices.add(Pair(i, "BUY"))
                } else if (prevFast >= prevSlow && currFast < currSlow) {
                    signalIndices.add(Pair(i, "SELL"))
                }
            }
        }

        val lastFast = fastSeries.lastOrNull { it != null } ?: 0.0
        val lastSlow = slowSeries.lastOrNull { it != null } ?: 0.0
        val lastClose = closes.last()

        val lastSignal = when {
            lastFast > lastSlow && lastClose >= lastFast -> "BUY"
            lastFast < lastSlow && lastClose <= lastFast -> "SELL"
            lastFast > lastSlow -> "BUY"
            lastFast < lastSlow -> "SELL"
            else -> "NEUTRAL"
        }

        val crossoverType = when {
            signalIndices.lastOrNull()?.second == "BUY" && (candles.size - 1 - (signalIndices.lastOrNull()?.first ?: 0)) <= 5 -> "GOLDEN_CROSS"
            signalIndices.lastOrNull()?.second == "SELL" && (candles.size - 1 - (signalIndices.lastOrNull()?.first ?: 0)) <= 5 -> "DEATH_CROSS"
            lastFast > lastSlow -> "BULLISH_TREND"
            lastFast < lastSlow -> "BEARISH_TREND"
            else -> "NEUTRAL"
        }

        return SmaSignalResult(
            signal = lastSignal,
            smaFast = lastFast,
            smaSlow = lastSlow,
            fastPeriod = fastPeriod,
            slowPeriod = slowPeriod,
            crossoverType = crossoverType,
            smaFastSeries = fastSeries,
            smaSlowSeries = slowSeries,
            signalIndices = signalIndices
        )
    }

    fun calculateFullStructureAnalysis(candles: List<Candle>): FullStructureAnalysis {
        if (candles.size < 20) {
            val emptyConfluence = SmcConfluenceBreakdown(
                correctMarketStructure = false,
                liquiditySweep = false,
                mssChochDetected = false,
                bosDetected = false,
                displacement = false,
                orderBlockDetected = false,
                fvgDetected = false,
                premiumDiscountValid = false,
                totalScore = 0,
                bias = "NEUTRAL",
                activeStep = "LIQUIDITY_SWEEP",
                invalidationSl = 0.0,
                targetLiquidityTp = 0.0
            )
            return FullStructureAnalysis(
                confluence = emptyConfluence,
                executionRecommendation = "INVALID / NO SETUP",
                summaryText = "Insufficient candle history to calculate SMC Full Structure."
            )
        }

        val swings = findSwingHighsLows(candles, 3)
        val highs = swings.highs
        val lows = swings.lows
        val lastCandle = candles.last()
        val currentPrice = lastCandle.close

        // 1. Market Direction (HH+HL -> Buy, LH+LL -> Sell)
        var hasHHHL = false
        var hasLHLL = false
        if (highs.size >= 2 && lows.size >= 2) {
            hasHHHL = highs.last().price > highs[highs.size - 2].price && lows.last().price > lows[lows.size - 2].price
            hasLHLL = highs.last().price < highs[highs.size - 2].price && lows.last().price < lows[lows.size - 2].price
        }

        var bias = "NEUTRAL"
        if (hasHHHL) bias = "BUY"
        else if (hasLHLL) bias = "SELL"
        else {
            val generalTrend = detectTrend(candles)
            if (generalTrend == "BULLISH") bias = "BUY"
            else if (generalTrend == "BEARISH") bias = "SELL"
        }

        val correctMarketStructure = hasHHHL || hasLHLL || (bias != "NEUTRAL")

        // 2. Liquidity Sweep Detection
        var liquiditySweep = false
        val recent15 = candles.takeLast(15)
        if (highs.isNotEmpty() || lows.isNotEmpty()) {
            val checkHighs = if (highs.size > 1) highs.dropLast(1) else highs
            val checkLows = if (lows.size > 1) lows.dropLast(1) else lows

            for (c in recent15) {
                for (sh in checkHighs) {
                    if (c.high > sh.price && c.close < sh.price) {
                        liquiditySweep = true
                        break
                    }
                }
                for (sl in checkLows) {
                    if (c.low < sl.price && c.close > sl.price) {
                        liquiditySweep = true
                        break
                    }
                }
                if (liquiditySweep) break
            }
        }

        // 3. Structure Shift (MSS / CHOCH)
        var mssChochDetected = false
        if (bias == "BUY" && highs.isNotEmpty()) {
            mssChochDetected = currentPrice > highs.last().price || (recent15.any { it.close > highs.last().price })
        } else if (bias == "SELL" && lows.isNotEmpty()) {
            mssChochDetected = currentPrice < lows.last().price || (recent15.any { it.close < lows.last().price })
        }

        // 4. BOS (Break of Structure)
        var bosDetected = false
        if (bias == "BUY" && highs.size >= 2) {
            bosDetected = currentPrice > highs.maxOf { it.price } * 0.999
        } else if (bias == "SELL" && lows.size >= 2) {
            bosDetected = currentPrice < lows.minOf { it.price } * 1.001
        }

        // 5. Displacement (Strong momentum candle)
        var displacement = false
        val avgBodySize = candles.takeLast(15).map { abs(it.close - it.open) }.average()
        for (c in candles.takeLast(5)) {
            val body = abs(c.close - c.open)
            if (body >= avgBodySize * 1.6) {
                displacement = true
                break
            }
        }

        // 6. Order Block (OB) Detection
        var orderBlockDetected = false
        var obRange: Pair<Double, Double>? = null
        if (candles.size >= 10) {
            val recent10 = candles.takeLast(10)
            if (bias == "BUY") {
                // Last down candle before impulse move
                val downCandle = recent10.findLast { it.close < it.open }
                if (downCandle != null) {
                    obRange = Pair(downCandle.low, downCandle.high)
                    // Check if current price retests OB
                    if (currentPrice >= downCandle.low * 0.998 && currentPrice <= downCandle.high * 1.005) {
                        orderBlockDetected = true
                    }
                }
            } else if (bias == "SELL") {
                // Last up candle before impulse move
                val upCandle = recent10.findLast { it.close > it.open }
                if (upCandle != null) {
                    obRange = Pair(upCandle.low, upCandle.high)
                    if (currentPrice <= upCandle.high * 1.002 && currentPrice >= upCandle.low * 0.995) {
                        orderBlockDetected = true
                    }
                }
            }
        }

        // 7. Fair Value Gap (FVG) Detection
        var fvgDetected = false
        var fvgRange: Pair<Double, Double>? = null
        for (i in (candles.size - 8) until (candles.size - 1)) {
            if (i >= 2) {
                val c1 = candles[i - 2]
                val c3 = candles[i]
                if (bias == "BUY" && c3.low > c1.high) {
                    fvgRange = Pair(c1.high, c3.low)
                    fvgDetected = true
                    break
                } else if (bias == "SELL" && c3.high < c1.low) {
                    fvgRange = Pair(c3.high, c1.low)
                    fvgDetected = true
                    break
                }
            }
        }

        // 8. Premium / Discount Check
        val swingMax = if (highs.isNotEmpty()) highs.maxOf { it.price } else candles.takeLast(30).maxOf { it.high }
        val swingMin = if (lows.isNotEmpty()) lows.minOf { it.price } else candles.takeLast(30).minOf { it.low }
        val equilibrium = (swingMax + swingMin) / 2.0

        var premiumDiscountValid = false
        if (bias == "BUY") {
            // Buy in Discount zone (< Equilibrium)
            premiumDiscountValid = currentPrice <= equilibrium
        } else if (bias == "SELL") {
            // Sell in Premium zone (> Equilibrium)
            premiumDiscountValid = currentPrice >= equilibrium
        }

        // Calculate Confluence Score according to requested weightage
        var score = 0
        if (correctMarketStructure) score += 1
        if (liquiditySweep) score += 2
        if (mssChochDetected) score += 2
        if (bosDetected) score += 1
        if (displacement) score += 1
        if (orderBlockDetected) score += 1
        if (fvgDetected) score += 1
        if (premiumDiscountValid) score += 1

        // Active Step Progression
        val activeStep = when {
            score >= 8 -> "ENTRY_READY"
            premiumDiscountValid -> "PREMIUM_DISCOUNT"
            orderBlockDetected || fvgDetected -> "OB_FVG_RETEST"
            bosDetected -> "BOS"
            displacement -> "DISPLACEMENT"
            mssChochDetected -> "MSS_CHOCH"
            liquiditySweep -> "LIQUIDITY_SWEEP"
            else -> "MARKET_DIRECTION"
        }

        // Recommendation
        val recommendation = when {
            score >= 7 -> "HIGH CONFLUENCE ENTRY"
            score >= 5 -> "WAIT FOR RETEST"
            else -> "INVALID / NO SETUP"
        }

        // SL & TP Levels
        val invalidationSl = if (bias == "BUY") swingMin * 0.998 else swingMax * 1.002
        val targetLiquidityTp = if (bias == "BUY") swingMax * 1.002 else swingMin * 0.998

        val breakdown = SmcConfluenceBreakdown(
            correctMarketStructure = correctMarketStructure,
            liquiditySweep = liquiditySweep,
            mssChochDetected = mssChochDetected,
            bosDetected = bosDetected,
            displacement = displacement,
            orderBlockDetected = orderBlockDetected,
            fvgDetected = fvgDetected,
            premiumDiscountValid = premiumDiscountValid,
            totalScore = score,
            bias = bias,
            activeStep = activeStep,
            orderBlockRange = obRange,
            fvgRange = fvgRange,
            invalidationSl = invalidationSl,
            targetLiquidityTp = targetLiquidityTp
        )

        val summary = "SMC Full Structure Analysis: Score $score/10 ($recommendation). Bias: $bias."

        return FullStructureAnalysis(
            confluence = breakdown,
            executionRecommendation = recommendation,
            summaryText = summary
        )
    }

    fun runConfirmationEngine(candles: List<Candle>): AnalysisResult {
        val smaSignal = calculateSmaSignalEngine(candles)
        val fullStructure = calculateFullStructureAnalysis(candles)
        if (candles.size < 30) {
            return AnalysisResult(
                direction = "NO TRADE",
                score = 0,
                details = ConfirmationDetails(
                    higherTimeframeTrend = false,
                    keyLevelConfirmed = false,
                    liquiditySweep = false,
                    bosChochDetected = false,
                    retestConfirmed = false,
                    momentumAligned = false,
                    riskRewardValid = false
                ),
                levels = null,
                trend = "NEUTRAL",
                smaSignal = smaSignal,
                fullStructure = fullStructure
            )
        }

        val lastClose = candles.last().close
        val trend = detectTrend(candles)
        val keyLevel = detectKeyLevel(candles, lastClose)
        val liqSweep = detectLiquiditySweep(candles)
        val boschoch = detectBOSCHoCH(candles, trend)
        val retest = detectRetest(candles, trend)
        val momentum = detectMomentum(candles)
        val levels = calcEntrySlTp(candles, trend)
        val rrValid = levels != null && levels.rr >= 2.0

        val details = ConfirmationDetails(
            higherTimeframeTrend = trend != "NEUTRAL",
            keyLevelConfirmed = keyLevel,
            liquiditySweep = liqSweep,
            bosChochDetected = boschoch,
            retestConfirmed = retest,
            momentumAligned = momentum,
            riskRewardValid = rrValid
        )

        var score = 0
        if (details.higherTimeframeTrend) score++
        if (details.keyLevelConfirmed) score++
        if (details.liquiditySweep) score++
        if (details.bosChochDetected) score++
        if (details.retestConfirmed) score++
        if (details.momentumAligned) score++
        if (details.riskRewardValid) score++

        var direction = "NO TRADE"
        if (score >= 5) {
            if (trend == "BULLISH") direction = "BUY"
            else if (trend == "BEARISH") direction = "SELL"
        }

        return AnalysisResult(
            direction = direction,
            score = score,
            details = details,
            levels = levels,
            trend = trend,
            smaSignal = smaSignal,
            fullStructure = fullStructure
        )
    }
}
