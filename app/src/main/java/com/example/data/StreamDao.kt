package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {
    @Query("SELECT * FROM stream_config WHERE id = 1")
    fun getStreamConfig(): Flow<StreamConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStreamConfig(config: StreamConfig)

    @Query("SELECT * FROM stream_history ORDER BY startTime DESC")
    fun getStreamHistory(): Flow<List<StreamHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: StreamHistory): Long

    @Query("UPDATE stream_history SET durationSeconds = :duration, isSuccessful = :success, logMessages = :logs WHERE id = :id")
    suspend fun updateHistorySession(id: Long, duration: Long, success: Boolean, logs: String)

    @Query("DELETE FROM stream_history")
    suspend fun clearHistory()
}
