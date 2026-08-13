package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.trading.Candle
import com.example.domain.trading.SmaSignalResult
import com.example.domain.trading.TradeLevels
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    levels: TradeLevels?,
    smaSignal: SmaSignalResult? = null,
    modifier: Modifier = Modifier
) {
    val chartBg = Color(0xFF0D1B2A)
    val borderColor = Color(0xFF334155)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val accentColor = Color(0xFF3B82F6)
    val fastSmaColor = Color(0xFF06B6D4) // Cyan for SMA 9
    val slowSmaColor = Color(0xFFA855F7) // Purple for SMA 21
    val textMuted = Color(0xFF475569)

    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(chartBg)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .testTag("chart_empty_box"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No chart data available",
                color = textMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    val visibleCandles = remember(candles) { candles.takeLast(40) }
    val candleStartIndex = candles.size - visibleCandles.size
    var scaleX by remember { mutableFloatStateOf(1f) }

    val maxPriceRaw = visibleCandles.maxOf { it.high }
    val minPriceRaw = visibleCandles.minOf { it.low }

    var maxPrice = maxPriceRaw
    var minPrice = minPriceRaw

    if (levels != null) {
        maxPrice = max(maxPrice, levels.tp3)
        minPrice = min(minPrice, levels.sl)
    }

    val rangePadding = (maxPrice - minPrice) * 0.1
    maxPrice += rangePadding
    minPrice -= rangePadding
    val priceRange = if (maxPrice - minPrice <= 0) 1.0 else maxPrice - minPrice

    fun formatPrice(p: Double): String {
        return when {
            p > 1000 -> String.format("%.2f", p)
            p > 10 -> String.format("%.4f", p)
            else -> String.format("%.5f", p)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(chartBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .testTag("candlestick_chart_container")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Main chart canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scaleX = (scaleX * zoom).coerceIn(0.6f, 2.5f)
                            }
                        }
                        .testTag("candlestick_canvas")
                ) {
                    val width = size.width
                    val height = size.height

                    fun priceToY(p: Double): Float {
                        return (height - ((p - minPrice) / priceRange) * height).toFloat()
                    }

                    // Grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = height * i / gridSteps
                        drawLine(
                            color = borderColor.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw candles
                    val candleCount = visibleCandles.size
                    val totalCandleSpace = width / candleCount
                    val candleWidth = max((totalCandleSpace * 0.7f * scaleX), 3f)

                    visibleCandles.forEachIndexed { index, candle ->
                        val centerX = (index + 0.5f) * totalCandleSpace
                        val isGreen = candle.close >= candle.open
                        val color = if (isGreen) greenColor else redColor

                        val highY = priceToY(candle.high)
                        val lowY = priceToY(candle.low)
                        val openY = priceToY(candle.open)
                        val closeY = priceToY(candle.close)

                        // Wick
                        drawLine(
                            color = color,
                            start = Offset(centerX, highY),
                            end = Offset(centerX, lowY),
                            strokeWidth = 2f
                        )

                        // Body
                        val bodyTop = min(openY, closeY)
                        val bodyBottom = max(openY, closeY)
                        val bodyHeight = max(bodyBottom - bodyTop, 2f)

                        drawRect(
                            color = color,
                            topLeft = Offset(centerX - candleWidth / 2f, bodyTop),
                            size = Size(candleWidth, bodyHeight)
                        )
                    }

                    // Draw SMA Overlay Lines (Fast & Slow SMAs)
                    if (smaSignal != null) {
                        // Fast SMA Line
                        if (smaSignal.smaFastSeries.isNotEmpty()) {
                            val fastPath = Path()
                            var started = false
                            visibleCandles.forEachIndexed { index, _ ->
                                val globalIdx = candleStartIndex + index
                                val smaVal = smaSignal.smaFastSeries.getOrNull(globalIdx)
                                if (smaVal != null) {
                                    val x = (index + 0.5f) * totalCandleSpace
                                    val y = priceToY(smaVal)
                                    if (!started) {
                                        fastPath.moveTo(x, y)
                                        started = true
                                    } else {
                                        fastPath.lineTo(x, y)
                                    }
                                }
                            }
                            if (started) {
                                drawPath(
                                    path = fastPath,
                                    color = fastSmaColor,
                                    style = Stroke(width = 3f)
                                )
                            }
                        }

                        // Slow SMA Line
                        if (smaSignal.smaSlowSeries.isNotEmpty()) {
                            val slowPath = Path()
                            var started = false
                            visibleCandles.forEachIndexed { index, _ ->
                                val globalIdx = candleStartIndex + index
                                val smaVal = smaSignal.smaSlowSeries.getOrNull(globalIdx)
                                if (smaVal != null) {
                                    val x = (index + 0.5f) * totalCandleSpace
                                    val y = priceToY(smaVal)
                                    if (!started) {
                                        slowPath.moveTo(x, y)
                                        started = true
                                    } else {
                                        slowPath.lineTo(x, y)
                                    }
                                }
                            }
                            if (started) {
                                drawPath(
                                    path = slowPath,
                                    color = slowSmaColor,
                                    style = Stroke(width = 3f)
                                )
                            }
                        }

                        // Draw Visual BUY / SELL Signal Indicators on Crossover points
                        smaSignal.signalIndices.forEach { (globalIdx, sigType) ->
                            val localIdx = globalIdx - candleStartIndex
                            if (localIdx in 0 until visibleCandles.size) {
                                val candle = visibleCandles[localIdx]
                                val centerX = (localIdx + 0.5f) * totalCandleSpace
                                if (sigType == "BUY") {
                                    val y = priceToY(candle.low) + 16f
                                    drawCircle(
                                        color = greenColor,
                                        radius = 8f,
                                        center = Offset(centerX, y)
                                    )
                                    val triangle = Path().apply {
                                        moveTo(centerX, y - 6f)
                                        lineTo(centerX - 5f, y + 4f)
                                        lineTo(centerX + 5f, y + 4f)
                                        close()
                                    }
                                    drawPath(path = triangle, color = Color.White)
                                } else if (sigType == "SELL") {
                                    val y = priceToY(candle.high) - 16f
                                    drawCircle(
                                        color = redColor,
                                        radius = 8f,
                                        center = Offset(centerX, y)
                                    )
                                    val triangle = Path().apply {
                                        moveTo(centerX, y + 6f)
                                        lineTo(centerX - 5f, y - 4f)
                                        lineTo(centerX + 5f, y - 4f)
                                        close()
                                    }
                                    drawPath(path = triangle, color = Color.White)
                                }
                            }
                        }
                    }

                    // Draw Trade Levels Lines
                    if (levels != null) {
                        val dashPath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Entry
                        val entryY = priceToY(levels.entry)
                        drawLine(
                            color = accentColor,
                            start = Offset(0f, entryY),
                            end = Offset(width, entryY),
                            strokeWidth = 2f,
                            pathEffect = dashPath
                        )

                        // SL
                        val slY = priceToY(levels.sl)
                        drawLine(
                            color = redColor,
                            start = Offset(0f, slY),
                            end = Offset(width, slY),
                            strokeWidth = 2f,
                            pathEffect = dashPath
                        )

                        // TP1, TP2, TP3
                        val tp1Y = priceToY(levels.tp1)
                        drawLine(
                            color = greenColor.copy(alpha = 0.7f),
                            start = Offset(0f, tp1Y),
                            end = Offset(width, tp1Y),
                            strokeWidth = 1.5f,
                            pathEffect = dashPath
                        )

                        val tp2Y = priceToY(levels.tp2)
                        drawLine(
                            color = greenColor,
                            start = Offset(0f, tp2Y),
                            end = Offset(width, tp2Y),
                            strokeWidth = 2f,
                            pathEffect = dashPath
                        )

                        val tp3Y = priceToY(levels.tp3)
                        drawLine(
                            color = greenColor,
                            start = Offset(0f, tp3Y),
                            end = Offset(width, tp3Y),
                            strokeWidth = 2f,
                            pathEffect = dashPath
                        )
                    }
                }

                // Top Overlay Legend Bar
                if (smaSignal != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xEE0F172A))
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(fastSmaColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SMA(9): ${formatPrice(smaSignal.smaFast)}",
                            color = fastSmaColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(slowSmaColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SMA(21): ${formatPrice(smaSignal.smaSlow)}",
                            color = slowSmaColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        val signalBg = when (smaSignal.signal) {
                            "BUY" -> greenColor
                            "SELL" -> redColor
                            else -> textMuted
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(signalBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("sma_chart_indicator_tag")
                        ) {
                            Text(
                                text = "SMA: ${smaSignal.signal}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Right vertical price axis
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0F172A))
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                val steps = 4
                for (i in steps downTo 0) {
                    val p = minPrice + (priceRange * i / steps)
                    Text(
                        text = formatPrice(p),
                        color = textMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
