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
import com.example.ui.components.FullStructureModuleCard
import com.example.ui.components.SignalCard
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GainzAnalyzerScreen(
    currentUser: UserProfile,
    marketRepository: MarketRepository,
    signalRepository: SignalRepository,
    onOpenSidebar: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedSymbol by remember { mutableStateOf("BTC/USD") }
    var selectedTimeframe by remember { mutableStateOf("15M") }
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
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)

    fun runGainzAnalysis() {
        isLoading = true
        errorMessage = null
        scope.launch {
            when (val result = marketRepository.fetchMarketCandles(selectedSymbol, selectedTimeframe)) {
                is MarketDataResult.Success -> {
                    candles = result.candles
                    val analysis = TradingAnalyzerEngine.runConfirmationEngine(result.candles)
                    analysisResult = analysis
                    isLoading = false

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
                            signal_time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
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
        runGainzAnalysis()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryDark)
            .testTag("gainz_analyzer_screen")
    ) {
        // Top Header
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
                        modifier = Modifier.testTag("gainz_open_sidebar_btn")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Sidebar", tint = textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(
                    Icons.Default.CandlestickChart,
                    contentDescription = "Gainz Analyzer Logo",
                    tint = greenColor,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GAINZ ANALYZER V2 ALPHA",
                        color = textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "PRO TRADING SIGNAL ENGINE",
                        color = greenColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            IconButton(
                onClick = { runGainzAnalysis() },
                modifier = Modifier.testTag("gainz_refresh_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Run Analysis", tint = textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Live Pair & Price Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardDark)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACTIVE PAIR", color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { symbolDropdownExpanded = true }
                                    .testTag("gainz_symbol_dropdown")
                            ) {
                                Text(
                                    text = selectedSymbol,
                                    color = textPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = accentBlue)
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
                                        modifier = Modifier.testTag("gainz_symbol_option_$sym")
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("LATEST MARKET PRICE", color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val lastPrice = candles.lastOrNull()?.close
                        Text(
                            text = if (lastPrice != null) {
                                if (lastPrice > 1000) String.format("%.2f", lastPrice) else String.format("%.4f", lastPrice)
                            } else "—",
                            color = greenColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.testTag("gainz_latest_price_text")
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) greenColor else cardDark)
                            .border(1.dp, if (isSelected) greenColor else borderColor, RoundedCornerShape(10.dp))
                            .clickable { selectedTimeframe = tf }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("gainz_timeframe_$tf"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            color = if (isSelected) primaryDark else textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // SMA Signal Engine Quick Indicator Bar
            val smaSig = analysisResult?.smaSignal
            if (smaSig != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardDark)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SMA SIGNAL ENGINE",
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SMA(9): ${if (smaSig.smaFast > 1000) String.format("%.2f", smaSig.smaFast) else String.format("%.4f", smaSig.smaFast)}",
                                color = Color(0xFF06B6D4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SMA(21): ${if (smaSig.smaSlow > 1000) String.format("%.2f", smaSig.smaSlow) else String.format("%.4f", smaSig.smaSlow)}",
                                color = Color(0xFFA855F7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val badgeColor = when (smaSig.signal) {
                        "BUY" -> greenColor
                        "SELL" -> redColor
                        else -> textSecondary
                    }
                    val badgeBg = when (smaSig.signal) {
                        "BUY" -> greenColor.copy(alpha = 0.2f)
                        "SELL" -> redColor.copy(alpha = 0.2f)
                        else -> borderColor
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("visual_sma_indicator_banner")
                    ) {
                        Text(
                            text = "INDICATOR: ${smaSig.signal}",
                            color = badgeColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Candlestick Chart
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardDark)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = greenColor, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Gainz Engine Analyzing Market Data...", color = textSecondary, fontSize = 13.sp)
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(redColor.copy(alpha = 0.1f))
                            .border(1.dp, redColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = redColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gainz Analyzer Connection Error", color = redColor, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(errorMessage ?: "An error occurred", color = textSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { runGainzAnalysis() },
                                colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Retry Analysis", color = primaryDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {
                    CandlestickChart(
                        candles = candles,
                        levels = analysisResult?.levels,
                        smaSignal = analysisResult?.smaSignal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signal Analysis Details
            SignalCard(
                result = analysisResult,
                symbol = selectedSymbol,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Full Structure SMC Module
            FullStructureModuleCard(
                fullStructure = analysisResult?.fullStructure,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
