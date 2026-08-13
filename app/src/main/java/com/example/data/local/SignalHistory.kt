package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signal_history")
data class SignalHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val owner_id: String?,
    val symbol_code: String,
    val timeframe: String,
    val direction: String, // "BUY", "SELL", "NO TRADE"
    val confirmation_score: Int,
    val entry_level: Double?,
    val stop_loss: Double?,
    val take_profit_1: Double?,
    val take_profit_2: Double?,
    val take_profit_3: Double?,
    val risk_reward_ratio: Double?,
    val signal_time: String
)
