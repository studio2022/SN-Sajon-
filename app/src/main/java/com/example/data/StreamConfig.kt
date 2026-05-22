package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_config")
data class StreamConfig(
    @PrimaryKey val id: Int = 1, // We hold standard single row configuration
    val sourceVideoUrl: String = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val fbRtmpUrl: String = "rtmps://live-api-s.facebook.com:443/rtmp/",
    val fbStreamKey: String = "",
    val ytRtmpUrl: String = "rtmp://a.rtmp.youtube.com/live2",
    val ytStreamKey: String = "",
    val customRtmpUrl: String = "",
    val useDualRestream: Boolean = true,
    val useFb: Boolean = true,
    val useYt: Boolean = false,
    val useCustom: Boolean = false,

    // Credit & Admin features
    val userCredits: Int = 50, // default credits
    val isAdminOn: Boolean = false, // starts as regular, admin can toggle
    val streamCost: Int = 10, // credit cost per stream
    val adminPin: String = "1234", // simple Admin PIN code
    val adminAnnouncement: String = "Welcome! Your Live Stream Studio is active. Boost your credits from the admin panel.",
    val appDownloadUrl: String = "https://ai.studio/build" // App download URL configured by admin
)

