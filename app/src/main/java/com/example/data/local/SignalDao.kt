package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signal_history ORDER BY id DESC")
    fun getAllSignalsFlow(): Flow<List<SignalHistory>>

    @Query("SELECT * FROM signal_history WHERE owner_id = :userId ORDER BY id DESC")
    fun getSignalsForUserFlow(userId: String): Flow<List<SignalHistory>>

    @Query("SELECT * FROM signal_history ORDER BY id DESC")
    suspend fun getAllSignals(): List<SignalHistory>

    @Query("SELECT * FROM signal_history WHERE owner_id = :userId ORDER BY id DESC")
    suspend fun getSignalsForUser(userId: String): List<SignalHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalHistory): Long
}
