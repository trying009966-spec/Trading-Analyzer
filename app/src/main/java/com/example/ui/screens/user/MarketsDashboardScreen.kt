package com.example.ui.screens.user

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SignalHistory
import com.example.data.local.UserProfile
import com.example.data.repository.MarketDataResult
import com.example.data.repository.MarketRepository
import com.example.data.repository.SignalRepository
import com.example.domain.trading.AnalysisResult
import com.example.domain.trading.Candle
import com.example.domain.trading.TradingAnalyzerEngine
import com.example.ui.components.CandlestickChart
import com.example.ui.components.SignalCard
import kotlinx.coroutines.launch

@Composable
fun MarketsDashboardScreen(
    currentUser: UserProfile,
    marketRepository: MarketRepository,
    signalRepository: SignalRepository,
    onOpenSidebar: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedSymbol by remember { mutableStateOf("BTC/USD") }
    var selectedTimeframe by remember { mutableStateOf("1H") }
    var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var symbolDropdownExpanded by remember { mutableStateOf(false) }

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

    fun fetchAndAnalyze() {
        isLoading = true
        errorMessage = null
        scope.launch {
            when (val result = marketRepository.fetchMarketCandles(selectedSymbol, selectedTimeframe)) {
                is MarketDataResult.Success -> {
                    candles = result.candles
                    val analysis = TradingAnalyzerEngine.runConfirmationEngine(result.candles)
                    analysisResult = analysis
                    isLoading = false

                    // Auto-save valid signals (score >= 5) to database
                    if (analysis.direction != "NO TRADE" && analysis.score >= 5 && analysis.levels != null) {
                        val signal = SignalHistory(
                            owner_id = currentUser.owner_id,
                            symbol_code = selectedSymbol,
                            timeframe = selectedTimeframe,
                            direction = analysis.direction,
                            confirmation_score = analysis.score,
                            entry_level = analysis.levels.entry,
                            stop_loss = analysis.levels.sl,
                            take_profit_1 = analysis.levels.tp1,
                            take_profit_2 = analysis.levels.tp2,
                            take_profit_3 = analysis.levels.tp3,
                            risk_reward_ratio = analysis.levels.rr,
                            signal_time = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())
                        )
                        signalRepository.saveSignal(signal)
                    }
                }
                is MarketDataResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                    candles = emptyList()
                    analysisResult = null
                }
            }
        }
    }

    LaunchedEffect(selectedSymbol, selectedTimeframe) {
        fetchAndAnalyze()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryDark)
    ) {
        // Top Toolbar Header
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
                if (onOpenSidebar != null) {
                    IconButton(
                        onClick = onOpenSidebar,
                        modifier = Modifier.testTag("open_sidebar_btn")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Sidebar", tint = textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(Icons.Default.CandlestickChart, contentDescription = null, tint = accentBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TRADING ANALYZER",
                    color = textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            IconButton(onClick = { fetchAndAnalyze() }, modifier = Modifier.testTag("refresh_market_data_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Symbol Selector & Price Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardDark)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Symbol", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { symbolDropdownExpanded = true }
                                    .testTag("symbol_selector_dropdown")
                            ) {
                                Text(
                                    text = selectedSymbol,
                                    color = textPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                            }

                            DropdownMenu(
                                expanded = symbolDropdownExpanded,
                                onDismissRequest = { symbolDropdownExpanded = false },
                                modifier = Modifier.background(cardDark)
                            ) {
                                marketRepository.supportedSymbols.forEach { sym ->
                                    DropdownMenuItem(
                                        text = { Text(sym, color = textPrimary, fontWeight = FontWeight.SemiBold) },
                                        onClick = {
                                            selectedSymbol = sym
                                            symbolDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("symbol_option_$sym")
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Latest Price", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        val lastPrice = candles.lastOrNull()?.close
                        Text(
                            text = if (lastPrice != null) {
                                if (lastPrice > 1000) String.format("%.2f", lastPrice) else String.format("%.4f", lastPrice)
                            } else "—",
                            color = textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("latest_price_text")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeframe Selector Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(marketRepository.supportedTimeframes) { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accentBlue else cardDark)
                            .border(1.dp, if (isSelected) accentBlue else borderColor, RoundedCornerShape(8.dp))
                            .clickable { selectedTimeframe = tf }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("timeframe_btn_$tf"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            color = if (isSelected) Color.White else textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Chart / Loading / Error
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardDark)
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = accentBlue, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Fetching market data...", color = textSecondary, fontSize = 13.sp)
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(redColor.copy(alpha = 0.1f))
                            .border(1.dp, redColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = redColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Market Data Error", color = redColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage ?: "An error occurred", color = textSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { fetchAndAnalyze() },
                                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Retry Connection")
                            }
                        }
                    }
                }
                else -> {
                    CandlestickChart(
                        candles = candles,
                        levels = analysisResult?.levels,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signal Card
            SignalCard(
                result = analysisResult,
                symbol = selectedSymbol,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
