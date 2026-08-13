package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class UserSession {
    object Unauthenticated : UserSession()
    data class User(val user: UserProfile) : UserSession()
    data class Admin(val adminEmail: String) : UserSession()
}

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("trading_analyzer_session", Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow<UserSession>(UserSession.Unauthenticated)
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    fun setAdminSession(adminEmail: String) {
        prefs.edit().apply {
            putString("session_type", "ADMIN")
            putString("admin_email", adminEmail)
            apply()
        }
        _sessionState.value = UserSession.Admin(adminEmail)
    }

    fun setUserSession(user: UserProfile) {
        prefs.edit().apply {
            putString("session_type", "USER")
            putString("user_owner_id", user.owner_id)
            putString("user_email", user.email)
            putString("user_name", user.full_name)
            putString("user_status", user.status)
            apply()
        }
        _sessionState.value = UserSession.User(user)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _sessionState.value = UserSession.Unauthenticated
    }

    fun restoreSession(users: List<UserProfile>) {
        val type = prefs.getString("session_type", null)
        if (type == "ADMIN") {
            val email = prefs.getString("admin_email", "") ?: ""
            _sessionState.value = UserSession.Admin(email)
        } else if (type == "USER") {
            val ownerId = prefs.getString("user_owner_id", null)
            val matched = users.find { it.owner_id == ownerId }
            if (matched != null) {
                _sessionState.value = UserSession.User(matched)
            } else {
                clearSession()
            }
        } else {
            _sessionState.value = UserSession.Unauthenticated
        }
    }
}
