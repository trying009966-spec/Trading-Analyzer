package com.example.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.launch

@Composable
fun AdminApiScreen(
    marketRepository: MarketRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isTesting by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf<Boolean?>(null) }

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

    val currentKey = BuildConfig.TWELVE_DATA_API_KEY
    val maskedKey = if (currentKey.length > 6) {
        currentKey.take(3) + "••••••••" + currentKey.takeLast(3)
    } else {
        "••••••••••••"
    }

    fun handleTestConnection() {
        isTesting = true
        connectionStatus = null
        scope.launch {
            val success = marketRepository.testApiConnection()
            isTesting = false
            connectionStatus = success
            if (success) {
                Toast.makeText(context, "Connection successful! Twelve Data API is responding.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Connection failed. Please check your TWELVE_DATA_API_KEY.", Toast.LENGTH_SHORT).show()
            }
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
            Icon(Icons.Default.Key, contentDescription = null, tint = accentBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Twelve Data API Settings",
                color = textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("admin_api_header")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardDark)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "CONNECTION STATUS",
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusIcon = when (connectionStatus) {
                                true -> Icons.Default.CheckCircle
                                false -> Icons.Default.Warning
                                else -> Icons.Default.Key
                            }

                            val statusColor = when (connectionStatus) {
                                true -> greenColor
                                false -> redColor
                                else -> textSecondary
                            }

                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (connectionStatus) {
                                    true -> "Connected & Active"
                                    false -> "Connection Failed"
                                    else -> "Not Tested"
                                },
                                color = statusColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { handleTestConnection() },
                            enabled = !isTesting,
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("test_api_connection_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Test Connection", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // API Security Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardDark)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = greenColor, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SECURE KEY STORAGE", color = greenColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Configured API Key:", color = textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = maskedKey,
                        color = textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("masked_api_key_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "The Twelve Data API key is configured safely using App Secrets and injected securely at build time via BuildConfig. It is never exposed in plaintext to regular users.",
                        color = textMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
