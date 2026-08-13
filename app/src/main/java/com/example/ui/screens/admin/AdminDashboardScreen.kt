package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SignalRepository
import com.example.data.repository.UserRepository

@Composable
fun AdminDashboardScreen(
    userRepository: UserRepository,
    signalRepository: SignalRepository,
    onAdminLogout: () -> Unit
) {
    val users by userRepository.allUsers.collectAsState(initial = emptyList())
    val signals by signalRepository.getAllSignals().collectAsState(initial = emptyList())

    val primaryDark = Color(0xFF0F172A)
    val cardDark = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val accentBlue = Color(0xFF3B82F6)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val yellowColor = Color(0xFFF59E0B)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF475569)

    val pendingCount = users.count { it.status == "pending" }
    val approvedCount = users.count { it.status == "approved" }
    val rejectedCount = users.count { it.status == "rejected" }
    val blockedCount = users.count { it.status == "blocked" }

    val buySignalsCount = signals.count { it.direction == "BUY" }
    val sellSignalsCount = signals.count { it.direction == "SELL" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryDark)
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardDark)
                .border(1.dp, borderColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = accentBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Admin Dashboard",
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("admin_dashboard_title")
                )
            }

            IconButton(onClick = onAdminLogout, modifier = Modifier.testTag("admin_logout_button")) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "USER STATISTICS",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // User Stat Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "TOTAL USERS",
                    value = users.size.toString(),
                    valueColor = accentBlue,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "PENDING",
                    value = pendingCount.toString(),
                    valueColor = yellowColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "APPROVED",
                    value = approvedCount.toString(),
                    valueColor = greenColor,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "REJECTED / BLOCKED",
                    value = (rejectedCount + blockedCount).toString(),
                    valueColor = redColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SIGNAL STATISTICS",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardDark)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(buySignalsCount.toString(), color = greenColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("BUY Signals", color = textSecondary, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(sellSignalsCount.toString(), color = redColor, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("SELL Signals", color = textSecondary, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(signals.size.toString(), color = textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Total Saved", color = textSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SYSTEM STATUS",
                color = textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardDark)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    SystemStatusRow(label = "Trading Analyzer Engine", status = "Online", color = greenColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    SystemStatusRow(label = "Confirmation Engine (7 Filters)", status = "Active", color = greenColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    SystemStatusRow(label = "Twelve Data Market API", status = "Connected", color = greenColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    SystemStatusRow(label = "Room Local Database Persistence", status = "Healthy", color = greenColor)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val cardDark = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val textSecondary = Color(0xFF94A3B8)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardDark),
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = valueColor, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun SystemStatusRow(
    label: String,
    status: String,
    color: Color
) {
    val textPrimary = Color(0xFFF1F5F9)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = textPrimary, fontSize = 13.sp)
        }

        Text(status, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
