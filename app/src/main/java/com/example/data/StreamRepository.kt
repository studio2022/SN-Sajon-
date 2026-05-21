package com.example.data

import kotlinx.coroutines.flow.Flow

class StreamRepository(private val streamDao: StreamDao) {

    val streamConfig: Flow<StreamConfig?> = streamDao.getStreamConfig()
    val streamHistory: Flow<List<StreamHistory>> = streamDao.getStreamHistory()

    suspend fun saveConfig(config: StreamConfig) {
        streamDao.saveStreamConfig(config)
    }

    suspend fun insertHistory(history: StreamHistory): Long {
        return streamDao.insertHistory(history)
    }

    suspend fun updateHistory(id: Long, duration: Long, success: Boolean, logs: String) {
        streamDao.updateHistorySession(id, duration, success, logs)
    }

    suspend fun clearAllHistory() {
        streamDao.clearHistory()
    }
}
