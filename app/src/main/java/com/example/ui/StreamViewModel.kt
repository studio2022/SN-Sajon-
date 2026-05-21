package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StreamConfig
import com.example.data.StreamRepository
import com.example.streaming.StreamingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StreamViewModel(private val repository: StreamRepository) : ViewModel() {

    // Persistent Configuration from Room database
    val streamConfig: StateFlow<StreamConfig?> = repository.streamConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // History and telemetry logs for Analytics Tab
    val streamHistory = repository.streamHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Direct read of ongoing streaming states from our Foreground Service's static companion flow!
    val isStreaming = StreamingService.isStreaming
    val systemLogs = StreamingService.logFlow
    val currentSpeed = StreamingService.speed
    val currentBitrate = StreamingService.bitrate
    val currentDuration = StreamingService.durationSeconds

    fun updateConfig(config: StreamConfig) {
        viewModelScope.launch {
            repository.saveConfig(config)
        }
    }

    fun startStreaming(context: Context) {
        val currentConf = streamConfig.value ?: StreamConfig()
        
        // Save before starting
        viewModelScope.launch {
            repository.saveConfig(currentConf)
        }

        val startIntent = Intent(context, StreamingService::class.java).apply {
            action = "START"
            putExtra("source_url", currentConf.sourceVideoUrl)

            // Primary endpoint
            var primary = ""
            var secondary: String? = null

            if (currentConf.useFb) {
                primary = "${currentConf.fbRtmpUrl.trim().ensureTrailingSlash()}${currentConf.fbStreamKey.trim()}"
                if (currentConf.useYt) {
                    secondary = "${currentConf.ytRtmpUrl.trim().ensureTrailingSlash()}${currentConf.ytStreamKey.trim()}"
                } else if (currentConf.useCustom) {
                    secondary = currentConf.customRtmpUrl.trim()
                }
            } else if (currentConf.useYt) {
                primary = "${currentConf.ytRtmpUrl.trim().ensureTrailingSlash()}${currentConf.ytStreamKey.trim()}"
                if (currentConf.useCustom) {
                    secondary = currentConf.customRtmpUrl.trim()
                }
            } else if (currentConf.useCustom) {
                primary = currentConf.customRtmpUrl.trim()
            } else {
                // Default fallback if nothing selected
                primary = currentConf.customRtmpUrl.trim().ifEmpty { "rtmp://localhost/live" }
            }

            putExtra("rtmp_url_1", primary)
            if (currentConf.useDualRestream) {
                putExtra("rtmp_url_2", secondary)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(startIntent)
        } else {
            context.startService(startIntent)
        }
    }

    fun stopStreaming(context: Context) {
        val stopIntent = Intent(context, StreamingService::class.java).apply {
            action = "STOP"
        }
        context.startService(stopIntent)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    private fun String.ensureTrailingSlash(): String {
        return if (this.endsWith("/")) this else "$this/"
    }

    // Modern factory for ViewModel instantiation
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StreamViewModel::class.java)) {
                val database = AppDatabase.getDatabase(context)
                val repository = StreamRepository(database.streamDao())
                @Suppress("UNCHECKED_CAST")
                return StreamViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
