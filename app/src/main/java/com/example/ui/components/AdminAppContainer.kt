package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MarketRepository
import com.example.data.repository.SignalRepository
import com.example.data.repository.UserRepository
import com.example.ui.screens.admin.AdminApiScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.admin.AdminUsersScreen

@Composable
fun AdminAppContainer(
    userRepository: UserRepository,
    signalRepository: SignalRepository,
    marketRepository: MarketRepository,
    onAdminLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val primaryDark = Color(0xFF0F172A)
    val cardDark = Color(0xFF1E293B)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFF1F5F9)
    val textMuted = Color(0xFF475569)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = cardDark,
                contentColor = textPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("admin_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Overview") },
                    label = { Text("Overview", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = accentBlue,
                        indicatorColor = accentBlue,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier.testTag("admin_nav_overview")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                    label = { Text("Users", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = accentBlue,
                        indicatorColor = accentBlue,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier.testTag("admin_nav_users")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Key, contentDescription = "API Keys") },
                    label = { Text("API Keys", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = accentBlue,
                        indicatorColor = accentBlue,
                        unselectedIconColor = textMuted,
                        unselectedTextColor = textMuted
                    ),
                    modifier = Modifier.testTag("admin_nav_api")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .background(primaryDark)
        ) {
            when (selectedTab) {
                0 -> AdminDashboardScreen(
                    userRepository = userRepository,
                    signalRepository = signalRepository,
                    onAdminLogout = onAdminLogout
                )
                1 -> AdminUsersScreen(
                    userRepository = userRepository
                )
                2 -> AdminApiScreen(
                    marketRepository = marketRepository
                )
            }
        }
    }
}
