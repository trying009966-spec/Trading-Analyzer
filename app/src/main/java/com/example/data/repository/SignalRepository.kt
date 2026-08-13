package com.example.data.repository

import com.example.data.local.SignalDao
import com.example.data.local.SignalHistory
import kotlinx.coroutines.flow.Flow

class SignalRepository(private val signalDao: SignalDao) {

    fun getSignalsForUser(userId: String): Flow<List<SignalHistory>> {
        return signalDao.getSignalsForUserFlow(userId)
    }

    fun getAllSignals(): Flow<List<SignalHistory>> {
        return signalDao.getAllSignalsFlow()
    }

    suspend fun saveSignal(signal: SignalHistory): Long {
        return signalDao.insertSignal(signal)
    }
}
