package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfile
import com.example.data.repository.MarketRepository
import com.example.data.repository.SignalRepository
import com.example.ui.screens.user.GainzAnalyzerScreen
import com.example.ui.screens.user.MarketsDashboardScreen
import com.example.ui.screens.user.ProfileScreen
import com.example.ui.screens.user.SignalHistoryScreen
import kotlinx.coroutines.launch

const val ROUTE_GAINZ_ANALYZER = "/gainz-analyzer"
const val ROUTE_MARKETS = "/markets"
const val ROUTE_SIGNALS = "/signals"
const val ROUTE_PROFILE = "/profile"

@Composable
fun AppNavigation(
    currentUser: UserProfile,
    marketRepository: MarketRepository,
    signalRepository: SignalRepository,
    onLogout: () -> Unit
) {
    UserAppContainer(
        currentUser = currentUser,
        marketRepository = marketRepository,
        signalRepository = signalRepository,
        onLogout = onLogout
    )
}

@Composable
fun UserAppContainer(
    currentUser: UserProfile,
    marketRepository: MarketRepository,
    signalRepository: SignalRepository,
    onLogout: () -> Unit
) {
    var currentRoute by remember { mutableStateOf(ROUTE_GAINZ_ANALYZER) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val primaryDark = Color(0xFF0F172A)
    val cardDark = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val accentBlue = Color(0xFF3B82F6)
    val greenColor = Color(0xFF10B981)
    val textPrimary = Color(0xFFF1F5F9)
    val textMuted = Color(0xFF94A3B8)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = cardDark,
                drawerContentColor = textPrimary,
                modifier = Modifier
                    .width(300.dp)
                    .testTag("navigation_sidebar")
            ) {
                // Sidebar Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryDark)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CandlestickChart,
                                contentDescription = null,
                                tint = greenColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TRADING SYSTEM",
                                color = textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentUser.email,
                            color = textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Sidebar Entries
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "GAINZ ANALYZER V2 ALPHA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    },
                    selected = currentRoute == ROUTE_GAINZ_ANALYZER,
                    onClick = {
                        currentRoute = ROUTE_GAINZ_ANALYZER
                        scope.launch { drawerState.close() }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CandlestickChart,
                            contentDescription = "GAINZ ANALYZER V2 ALPHA"
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = greenColor.copy(alpha = 0.2f),
                        selectedIconColor = greenColor,
                        selectedTextColor = greenColor,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("sidebar_item_gainz_analyzer")
                )

                NavigationDrawerItem(
                    label = { Text("Markets", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    selected = currentRoute == ROUTE_MARKETS,
                    onClick = {
                        currentRoute = ROUTE_MARKETS
                        scope.launch { drawerState.close() }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Markets"
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = accentBlue.copy(alpha = 0.2f),
                        selectedIconColor = accentBlue,
                        selectedTextColor = accentBlue,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("sidebar_item_markets")
                )

                NavigationDrawerItem(
                    label = { Text("Signals", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    selected = currentRoute == ROUTE_SIGNALS,
                    onClick = {
                        currentRoute = ROUTE_SIGNALS
                        scope.launch { drawerState.close() }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Signals"
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = accentBlue.copy(alpha = 0.2f),
                        selectedIconColor = accentBlue,
                        selectedTextColor = accentBlue,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("sidebar_item_signals")
                )

                NavigationDrawerItem(
                    label = { Text("Profile", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    selected = currentRoute == ROUTE_PROFILE,
                    onClick = {
                        currentRoute = ROUTE_PROFILE
                        scope.launch { drawerState.close() }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = accentBlue.copy(alpha = 0.2f),
                        selectedIconColor = accentBlue,
                        selectedTextColor = accentBlue,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("sidebar_item_profile")
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = cardDark,
                    contentColor = textPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("user_bottom_navigation")
                ) {
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_GAINZ_ANALYZER,
                        onClick = { currentRoute = ROUTE_GAINZ_ANALYZER },
                        icon = { Icon(Icons.Default.CandlestickChart, contentDescription = "GAINZ ANALYZER V2 ALPHA") },
                        label = { Text("Gainz V2", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = greenColor,
                            indicatorColor = greenColor,
                            unselectedIconColor = textMuted,
                            unselectedTextColor = textMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_gainz_analyzer")
                    )

                    NavigationBarItem(
                        selected = currentRoute == ROUTE_MARKETS,
                        onClick = { currentRoute = ROUTE_MARKETS },
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = "Markets") },
                        label = { Text("Markets", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = accentBlue,
                            indicatorColor = accentBlue,
                            unselectedIconColor = textMuted,
                            unselectedTextColor = textMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_markets")
                    )

                    NavigationBarItem(
                        selected = currentRoute == ROUTE_SIGNALS,
                        onClick = { currentRoute = ROUTE_SIGNALS },
                        icon = { Icon(Icons.Default.History, contentDescription = "Signals") },
                        label = { Text("Signals", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = accentBlue,
                            indicatorColor = accentBlue,
                            unselectedIconColor = textMuted,
                            unselectedTextColor = textMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_signals")
                    )

                    NavigationBarItem(
                        selected = currentRoute == ROUTE_PROFILE,
                        onClick = { currentRoute = ROUTE_PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = accentBlue,
                            indicatorColor = accentBlue,
                            unselectedIconColor = textMuted,
                            unselectedTextColor = textMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(primaryDark)
            ) {
                when (currentRoute) {
                    ROUTE_GAINZ_ANALYZER -> GainzAnalyzerScreen(
                        currentUser = currentUser,
                        marketRepository = marketRepository,
                        signalRepository = signalRepository,
                        onOpenSidebar = { scope.launch { drawerState.open() } }
                    )
                    ROUTE_MARKETS -> MarketsDashboardScreen(
                        currentUser = currentUser,
                        marketRepository = marketRepository,
                        signalRepository = signalRepository,
                        onOpenSidebar = { scope.launch { drawerState.open() } }
                    )
                    ROUTE_SIGNALS -> SignalHistoryScreen(
                        currentUser = currentUser,
                        signalRepository = signalRepository,
                        onOpenSidebar = { scope.launch { drawerState.open() } }
                    )
                    ROUTE_PROFILE -> ProfileScreen(
                        user = currentUser,
                        onLogout = onLogout,
                        onOpenSidebar = { scope.launch { drawerState.open() } }
                    )
                    else -> GainzAnalyzerScreen(
                        currentUser = currentUser,
                        marketRepository = marketRepository,
                        signalRepository = signalRepository,
                        onOpenSidebar = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}
