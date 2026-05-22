package com.example.streaming

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.StreamHistory
import com.example.data.StreamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StreamingService : Service() {

    companion object {
        private const val TAG = "StreamingService"
        private const val CHANNEL_ID = "stream_service_channel"
        private const val NOTIFICATION_ID = 404

        private val _isStreaming = MutableStateFlow(false)
        val isStreaming = _isStreaming.asStateFlow()

        private val _logFlow = MutableStateFlow<List<String>>(emptyList())
        val logFlow = _logFlow.asStateFlow()

        private val _speed = MutableStateFlow(0.0)
        val speed = _speed.asStateFlow()

        private val _bitrate = MutableStateFlow(0.0)
        val bitrate = _bitrate.asStateFlow()

        private val _durationSeconds = MutableStateFlow(0L)
        val durationSeconds = _durationSeconds.asStateFlow()
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var durationJob: Job? = null
    private var sessionHistoryId: Long = -1L
    private lateinit var repository: StreamRepository

    private val logsList = mutableListOf<String>()

    inner class LocalBinder : Binder() {
        fun getService(): StreamingService = this@StreamingService
    }

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = StreamRepository(database.streamDao())
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val sourceUrl = intent?.getStringExtra("source_url") ?: ""
        val rtmpUrl1 = intent?.getStringExtra("rtmp_url_1") ?: ""
        val rtmpUrl2 = intent?.getStringExtra("rtmp_url_2")

        if (action == "START") {
            startStreamingSession(sourceUrl, rtmpUrl1, rtmpUrl2)
        } else if (action == "STOP") {
            stopStreamingSession()
        }

        return START_NOT_STICKY
    }

    private fun startStreamingSession(sourceUrl: String, rtmpUrl1: String, rtmpUrl2: String?) {
        if (_isStreaming.value) return
        _isStreaming.value = true
        logsList.clear()
        _logFlow.value = emptyList()
        _speed.value = 1.0
        _bitrate.value = 0.0
        _durationSeconds.value = 0L

        addLog("Initializing dual-channel restreaming engine...")
        addLog("Source Video Link: $sourceUrl")
        addLog("Stream Destination 1: $rtmpUrl1")
        rtmpUrl2?.let { addLog("Stream Destination 2: $it") }

        startForegroundNotification()

        serviceScope.launch {
            val history = StreamHistory(
                title = "Restream Session",
                sourceUrl = sourceUrl,
                rtmpOutputs = "$rtmpUrl1${if (!rtmpUrl2.isNullOrBlank()) ", $rtmpUrl2" else ""}",
                startTime = System.currentTimeMillis()
            )
            sessionHistoryId = repository.insertHistory(history)
        }

        durationJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                _durationSeconds.value += 1
                updateNotification()
            }
        }

        FFmpegStreamingExecutor.startRestream(sourceUrl, rtmpUrl1, rtmpUrl2, object : FFmpegStreamingExecutor.StreamCallback {
            override fun onLog(message: String) {
                addLog(message)
            }

            override fun onStatistics(speed: Double, bitrate: Double, timeMs: Long) {
                _speed.value = speed
                _bitrate.value = bitrate
            }

            override fun onComplete(isSuccess: Boolean, returnCode: Int) {
                addLog("Execution session completed. Return Code: $returnCode")
                serviceScope.launch {
                    if (sessionHistoryId != -1L) {
                        repository.updateHistory(
                            id = sessionHistoryId,
                            duration = _durationSeconds.value,
                            success = isSuccess,
                            logs = logsList.takeLast(100).joinToString("\n")
                        )
                    }
                    stopSelf()
                }
            }
        })
    }

    private fun startForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activeDuration = _durationSeconds.value
        val min = activeDuration / 60
        val sec = activeDuration % 60
        val bodyText = "Broadcasting Live: %02d:%02d | %.0fkBps | %.2fx Speed".format(min, sec, _bitrate.value, _speed.value)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Live Stream Studio - Active")
            .setContentText(bodyText)
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun addLog(msg: String) {
        logsList.add(msg)
        if (logsList.size > 100) {
            logsList.removeAt(0)
        }
        _logFlow.value = logsList.toList()
        Log.d(TAG, msg)
    }

    private fun stopStreamingSession() {
        if (!_isStreaming.value) return
        addLog("Terminating Streaming session request received.")
        durationJob?.cancel()
        durationJob = null

        FFmpegStreamingExecutor.stopRestream()

        _isStreaming.value = false
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Live Stream Activity Log Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for display streaming parameters of ongoing broadcast"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        durationJob?.cancel()
        FFmpegStreamingExecutor.stopRestream()
        _isStreaming.value = false
        serviceScope.cancel()
    }
}
