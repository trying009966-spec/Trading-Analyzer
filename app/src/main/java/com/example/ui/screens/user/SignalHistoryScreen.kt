package com.example.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SignalHistory
import com.example.data.local.UserProfile
import com.example.data.repository.SignalRepository

@Composable
fun SignalHistoryScreen(
    currentUser: UserProfile,
    signalRepository: SignalRepository,
    onOpenSidebar: (() -> Unit)? = null
) {
    val signals by signalRepository.getSignalsForUser(currentUser.owner_id).collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("ALL") }

    val primaryDark = Color(0xFF0F172A)
    val cardDark = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val accentBlue = Color(0xFF3B82F6)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF475569)

    val filters = listOf("ALL", "BUY", "SELL", "BTC/USD", "EUR/USD", "XAU/USD")

    val filteredSignals = remember(signals, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> signals
            "BUY" -> signals.filter { it.direction == "BUY" }
            "SELL" -> signals.filter { it.direction == "SELL" }
            else -> signals.filter { it.symbol_code == selectedFilter }
        }
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
        modifier = Modifier
            .fillMaxSize()
            .background(primaryDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardDark)
                .border(1.dp, borderColor)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onOpenSidebar != null) {
                IconButton(onClick = onOpenSidebar, modifier = Modifier.testTag("signal_open_sidebar_btn")) {
                    Icon(Icons.Default.Menu, contentDescription = "Sidebar", tint = textPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(Icons.Default.History, contentDescription = null, tint = accentBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Signal History",
                color = textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("signal_history_header")
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accentBlue else cardDark)
                            .border(1.dp, if (isSelected) accentBlue else borderColor, RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("filter_chip_$filter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredSignals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = textMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No signals recorded yet", color = textSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Valid setups (score ≥ 5/7) will appear in history", color = textMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSignals, key = { it.id }) { item ->
                        SignalHistoryItem(item = item, formatPrice = ::formatPrice)
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalHistoryItem(
    item: SignalHistory,
    formatPrice: (Double?) -> String
) {
    val cardDark = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val accentBlue = Color(0xFF3B82F6)
    val yellowColor = Color(0xFFF59E0B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF475569)

    val dirColor = if (item.direction == "BUY") greenColor else redColor
    val dirBg = if (item.direction == "BUY") greenColor.copy(alpha = 0.15f) else redColor.copy(alpha = 0.15f)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .testTag("signal_history_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.symbol_code, color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text(item.signal_time, color = textMuted, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(dirBg)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(item.direction, color = dirColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${item.confirmation_score}/7",
                        color = dirColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entry: ${formatPrice(item.entry_level)}", color = accentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("SL: ${formatPrice(item.stop_loss)}", color = redColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("TP1: ${formatPrice(item.take_profit_1)}", color = greenColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("R:R: 1:${String.format("%.1f", item.risk_reward_ratio ?: 0.0)}", color = yellowColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
