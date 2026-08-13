package com.example.ui.screens.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.local.UserProfile
import com.example.data.repository.UserRepository
import kotlinx.coroutines.launch

@Composable
fun AdminUsersScreen(
    userRepository: UserRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val users by userRepository.allUsers.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("ALL") }

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

    val filters = listOf("ALL", "pending", "approved", "rejected", "blocked")

    val filteredUsers = remember(users, selectedFilter) {
        if (selectedFilter == "ALL") users
        else users.filter { it.status.lowercase() == selectedFilter.lowercase() }
    }

    fun handleStatusChange(user: UserProfile, newStatus: String) {
        scope.launch {
            userRepository.updateUserStatus(user.owner_id, newStatus)
            Toast.makeText(context, "Updated ${user.full_name}'s status to $newStatus.", Toast.LENGTH_SHORT).show()
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
            Icon(Icons.Default.People, contentDescription = null, tint = accentBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "User Management",
                color = textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("admin_users_header")
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Status Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter.uppercase() == selectedFilter.uppercase()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accentBlue else cardDark)
                            .border(1.dp, if (isSelected) accentBlue else borderColor, RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("admin_user_filter_$filter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.uppercase(),
                            color = if (isSelected) Color.White else textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No users found in status '$selectedFilter'", color = textSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUsers, key = { it.owner_id }) { user ->
                        UserManagementCard(
                            user = user,
                            onStatusChange = ::handleStatusChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserManagementCard(
    user: UserProfile,
    onStatusChange: (UserProfile, String) -> Unit
) {
    val cardDark = Color(0xFF1E293B)
    val borderColor = Color(0xFF334155)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val yellowColor = Color(0xFFF59E0B)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF94A3B8)
    val textMuted = Color(0xFF475569)

    val statusColor = when (user.status.lowercase()) {
        "approved" -> greenColor
        "pending" -> yellowColor
        "rejected" -> redColor
        "blocked" -> redColor
        else -> textMuted
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .testTag("admin_user_card_${user.owner_id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.full_name, color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text(user.email, color = textSecondary, fontSize = 13.sp)
                    Text("Registered: ${user.registration_date}", color = textMuted, fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = user.status.uppercase(),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.status != "approved") {
                    Button(
                        onClick = { onStatusChange(user, "approved") },
                        colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_approve_btn_${user.owner_id}")
                    ) {
                        Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (user.status != "rejected") {
                    OutlinedButton(
                        onClick = { onStatusChange(user, "rejected") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, redColor),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = redColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_reject_btn_${user.owner_id}")
                    ) {
                        Text("Reject", color = redColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (user.status != "blocked") {
                    OutlinedButton(
                        onClick = { onStatusChange(user, "blocked") },
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .testTag("admin_block_btn_${user.owner_id}")
                    ) {
                        Text("Block", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
