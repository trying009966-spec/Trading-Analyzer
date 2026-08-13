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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.domain.trading.FullStructureAnalysis

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullStructureModuleCard(
    fullStructure: FullStructureAnalysis?,
    modifier: Modifier = Modifier
) {
    if (fullStructure == null) return

    val cardBg = Color(0xFF0F172A)
    val borderColor = Color(0xFF1E293B)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val yellowColor = Color(0xFFF59E0B)
    val cyanColor = Color(0xFF06B6D4)
    val purpleColor = Color(0xFFA855F7)
    val textPrimary = Color(0xFFF8FAFC)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF64748B)

    val c = fullStructure.confluence
    val rec = fullStructure.executionRecommendation

    val recColor = when (rec) {
        "HIGH CONFLUENCE ENTRY" -> greenColor
        "WAIT FOR RETEST" -> yellowColor
        else -> redColor
    }

    val recBg = recColor.copy(alpha = 0.15f)

    fun formatPrice(p: Double?): String {
        if (p == null || p == 0.0) return "N/A"
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
            .padding(16.dp)
            .testTag("full_structure_module_card")
    ) {
        // Module Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(purpleColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "SMC Icon",
                        tint = purpleColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FULL STRUCTURE SMC ENGINE",
                        color = textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "ICT / Smart Money Concepts Entry Model",
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(recBg)
                    .border(1.dp, recColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("smc_recommendation_badge")
            ) {
                Text(
                    text = rec,
                    color = recColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Confluence Score Banner (Max 10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SMC CONFLUENCE SCORE",
                    color = textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${c.totalScore}",
                        color = recColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.testTag("smc_total_score_text")
                    )
                    Text(
                        text = "/10 Points",
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "MARKET BIAS",
                    color = textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val biasColor = if (c.bias == "BUY") greenColor else if (c.bias == "SELL") redColor else textMuted
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(biasColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = c.bias,
                        color = biasColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7-Step Simplified Entry Pipeline Flow
        Text(
            text = "SIMPLIFIED ENTRY MODEL PIPELINE",
            color = textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        val pipelineSteps = listOf(
            Triple("Liquidity Sweep", c.liquiditySweep, "+2 pts"),
            Triple("MSS / CHoCH", c.mssChochDetected, "+2 pts"),
            Triple("Displacement", c.displacement, "+1 pt"),
            Triple("BOS", c.bosDetected, "+1 pt"),
            Triple("OB/FVG Retest", c.orderBlockDetected || c.fvgDetected, "+2 pts"),
            Triple("Prem/Disc", c.premiumDiscountValid, "+1 pt"),
            Triple("ENTRY", c.totalScore >= 6, "Ready")
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            pipelineSteps.forEachIndexed { idx, (stepName, isPassed, weight) ->
                val bg = if (isPassed) greenColor.copy(alpha = 0.2f) else Color(0xFF1E293B)
                val borderC = if (isPassed) greenColor else borderColor
                val textC = if (isPassed) greenColor else textMuted

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .border(1.dp, borderC, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = textC,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stepName,
                        color = if (isPassed) textPrimary else textMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isPassed) FontWeight.Bold else FontWeight.Normal
                    )
                    if (idx < pipelineSteps.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = borderColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Confluence Breakdown Checklist
        Text(
            text = "CONFLUENCE MATRIX BREAKDOWN",
            color = textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        val matrixItems = listOf(
            Triple("Market Structure (HH+HL / LH+LL)", c.correctMarketStructure, "+1"),
            Triple("Liquidity Sweep (Prev/Equal Low/High)", c.liquiditySweep, "+2"),
            Triple("MSS / CHoCH (Structure Shift)", c.mssChochDetected, "+2"),
            Triple("Break of Structure (BOS)", c.bosDetected, "+1"),
            Triple("Displacement Candle (Momentum)", c.displacement, "+1"),
            Triple("Order Block (OB Retest Zone)", c.orderBlockDetected, "+1"),
            Triple("Fair Value Gap (FVG Imbalance)", c.fvgDetected, "+1"),
            Triple("Premium / Discount Check", c.premiumDiscountValid, "+1")
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            matrixItems.forEach { (label, active, points) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (active) greenColor else redColor.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (active) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            color = if (active) textPrimary else textMuted,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }

                    Text(
                        text = if (active) points else "0",
                        color = if (active) greenColor else textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = borderColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Levels & Range Zones (Invalidation SL & Target TP)
        Text(
            text = "STRUCTURE LEVELS & INVALIDATION",
            color = textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "INVALIDATION SL",
                    color = textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(c.invalidationSl),
                    color = redColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEXT LIQUIDITY TP",
                    color = textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatPrice(c.targetLiquidityTp),
                    color = greenColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (c.orderBlockRange != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ORDER BLOCK ZONE",
                        color = textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatPrice(c.orderBlockRange.first)} - ${formatPrice(c.orderBlockRange.second)}",
                        color = cyanColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
