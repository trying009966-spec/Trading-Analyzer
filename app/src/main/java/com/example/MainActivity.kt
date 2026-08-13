package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.local.UserProfile
import com.example.data.repository.MarketRepository
import com.example.data.repository.SessionManager
import com.example.data.repository.SignalRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.UserSession
import com.example.ui.components.AdminAppContainer
import com.example.ui.components.UserAppContainer
import com.example.ui.screens.auth.AdminLoginScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.PendingScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.RejectedScreen
import com.example.ui.theme.TradingAnalyzerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(db.userDao())
        val signalRepository = SignalRepository(db.signalDao())
        val marketRepository = MarketRepository()
        val sessionManager = SessionManager(this)

        setContent {
            TradingAnalyzerTheme {
                TradingAnalyzerApp(
                    userRepository = userRepository,
                    signalRepository = signalRepository,
                    marketRepository = marketRepository,
                    sessionManager = sessionManager
                )
            }
        }
    }
}

@Composable
fun TradingAnalyzerApp(
    userRepository: UserRepository,
    signalRepository: SignalRepository,
    marketRepository: MarketRepository,
    sessionManager: SessionManager
) {
    val navController = rememberNavController()
    val sessionState by sessionManager.sessionState.collectAsState()
    val allUsers by userRepository.allUsers.collectAsState(initial = null)

    val primaryDark = Color(0xFF0F172A)

    LaunchedEffect(allUsers) {
        val usersList = allUsers
        if (usersList != null) {
            sessionManager.restoreSession(usersList)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = primaryDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(primaryDark)
        ) {
            if (allUsers == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else {
                when (val currentSession = sessionState) {
                    is UserSession.Admin -> {
                        AdminAppContainer(
                            userRepository = userRepository,
                            signalRepository = signalRepository,
                            marketRepository = marketRepository,
                            onAdminLogout = { sessionManager.clearSession() }
                        )
                    }

                    is UserSession.User -> {
                        val userStatus = currentSession.user.status.lowercase()
                        when (userStatus) {
                            "approved" -> {
                                UserAppContainer(
                                    currentUser = currentSession.user,
                                    marketRepository = marketRepository,
                                    signalRepository = signalRepository,
                                    onLogout = { sessionManager.clearSession() }
                                )
                            }
                            "pending" -> {
                                PendingScreen(
                                    onLogout = { sessionManager.clearSession() }
                                )
                            }
                            "rejected" -> {
                                RejectedScreen(
                                    onLogout = { sessionManager.clearSession() }
                                )
                            }
                            "blocked" -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RejectedScreen(
                                        onLogout = { sessionManager.clearSession() }
                                    )
                                }
                            }
                            else -> {
                                PendingScreen(
                                    onLogout = { sessionManager.clearSession() }
                                )
                            }
                        }
                    }

                    is UserSession.Unauthenticated -> {
                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {
                            composable("login") {
                                LoginScreen(
                                    userRepository = userRepository,
                                    onLoginSuccess = { user ->
                                        sessionManager.setUserSession(user)
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate("register")
                                    },
                                    onNavigateToAdminLogin = {
                                        navController.navigate("admin_login")
                                    }
                                )
                            }

                            composable("register") {
                                RegisterScreen(
                                    userRepository = userRepository,
                                    onRegisterSuccess = {
                                        navController.popBackStack()
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable("admin_login") {
                                AdminLoginScreen(
                                    onAdminLoginSuccess = { adminEmail ->
                                        sessionManager.setAdminSession(adminEmail)
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
