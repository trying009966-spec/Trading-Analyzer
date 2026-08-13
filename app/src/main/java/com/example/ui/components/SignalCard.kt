package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.trading.AnalysisResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignalCard(
    result: AnalysisResult?,
    symbol: String,
    modifier: Modifier = Modifier
) {
    if (result == null) return

    val cardBg = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val yellowColor = Color(0xFFF59E0B)
    val accentColor = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF475569)

    val direction = result.direction
    val score = result.score
    val details = result.details
    val levels = result.levels

    val dirColor = when (direction) {
        "BUY" -> greenColor
        "SELL" -> redColor
        else -> textMuted
    }

    val dirBg = when (direction) {
        "BUY" -> greenColor.copy(alpha = 0.15f)
        "SELL" -> redColor.copy(alpha = 0.15f)
        else -> borderColor
    }

    val strength = when {
        score >= 7 -> "VERY STRONG"
        score >= 6 -> "STRONG"
        score >= 5 -> "VALID SETUP"
        else -> "NO TRADE"
    }

    val strengthColor = when {
        score >= 7 -> greenColor
        score >= 6 -> Color(0xFF6EE7B7)
        score >= 5 -> yellowColor
        else -> textMuted
    }

    fun formatPrice(p: Double?): String {
        if (p == null) return "N/A"
        return when {
            p > 1000 -> String.format("%.2f", p)
            p > 10 -> String.format("%.4f", p)
            else -> String.format("%.5f", p)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("signal_card")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(dirBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = symbol,
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = direction,
                    color = dirColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("signal_direction_text")
                )
                Text(
                    text = strength,
                    color = strengthColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$score/7",
                    color = dirColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("signal_score_text")
                )
                Text(
                    text = "Confirmation Score",
                    color = textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Algorithmic SMA Signal Engine Section
        val sma = result.smaSignal
        if (sma != null) {
            HorizontalDivider(color = borderColor, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALGORITHMIC SMA SIGNAL ENGINE",
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val smaSigColor = when (sma.signal) {
                        "BUY" -> greenColor
                        "SELL" -> redColor
                        else -> textMuted
                    }
                    val smaSigBg = when (sma.signal) {
                        "BUY" -> greenColor.copy(alpha = 0.2f)
                        "SELL" -> redColor.copy(alpha = 0.2f)
                        else -> borderColor
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(smaSigBg)
                            .border(1.dp, smaSigColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .testTag("sma_signal_indicator_badge")
                    ) {
                        Text(
                            text = "SMA ${sma.signal}",
                            color = smaSigColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LevelItem(
                        label = "FAST SMA (${sma.fastPeriod})",
                        value = formatPrice(sma.smaFast),
                        valueColor = Color(0xFF06B6D4)
                    )
                    LevelItem(
                        label = "SLOW SMA (${sma.slowPeriod})",
                        value = formatPrice(sma.smaSlow),
                        valueColor = Color(0xFFA855F7)
                    )
                    LevelItem(
                        label = "CROSSOVER STATUS",
                        value = sma.crossoverType.replace("_", " "),
                        valueColor = if (sma.signal == "BUY") greenColor else if (sma.signal == "SELL") redColor else yellowColor
                    )
                }
            }
        }

        // Confirmations section
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONFIRMATIONS",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val confirmItems = listOf(
                Pair(details.higherTimeframeTrend, "Higher TF Trend"),
                Pair(details.keyLevelConfirmed, "Key Level"),
                Pair(details.liquiditySweep, "Liquidity Sweep"),
                Pair(details.bosChochDetected, "BOS / CHoCH"),
                Pair(details.retestConfirmed, "Retest"),
                Pair(details.momentumAligned, "Momentum"),
                Pair(details.riskRewardValid, "R:R ≥ 1:2")
            )

            confirmItems.forEach { (confirmed, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = if (confirmed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (confirmed) greenColor else redColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = if (confirmed) textPrimary else textMuted,
                        fontSize = 13.sp,
                        fontWeight = if (confirmed) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Trade Levels Section
        if (levels != null) {
            HorizontalDivider(color = borderColor, thickness = 1.dp)

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TRADE LEVELS",
                    color = textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LevelItem(label = "ENTRY", value = formatPrice(levels.entry), valueColor = accentColor)
                    LevelItem(label = "STOP LOSS", value = formatPrice(levels.sl), valueColor = redColor)
                    LevelItem(label = "TP1", value = formatPrice(levels.tp1), valueColor = greenColor)
                    LevelItem(label = "TP2", value = formatPrice(levels.tp2), valueColor = greenColor)
                    LevelItem(label = "TP3", value = formatPrice(levels.tp3), valueColor = greenColor)
                    LevelItem(label = "R:R", value = "1:" + String.format("%.1f", levels.rr), valueColor = yellowColor)
                }
            }
        }
    }
}

@Composable
private fun LevelItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(modifier = Modifier.width(95.dp)) {
        Text(
            text = label,
            color = Color(0xFF475569),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
