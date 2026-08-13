package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val owner_id: String,
    val full_name: String,
    val email: String,
    val password_hash: String,
    val status: String = "pending", // "pending", "approved", "rejected", "blocked"
    val registration_date: String
)
