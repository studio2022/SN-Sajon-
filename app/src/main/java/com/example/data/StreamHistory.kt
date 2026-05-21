package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_history")
data class StreamHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val sourceUrl: String,
    val rtmpOutputs: String, // Comma-separated output servers
    val startTime: Long,
    val durationSeconds: Long = 0L,
    val isSuccessful: Boolean = true,
    val logMessages: String = ""
)
