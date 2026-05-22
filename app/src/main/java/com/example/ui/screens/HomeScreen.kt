package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.StreamConfig
import com.example.ui.StreamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun Modifier.neonGlowBorder(
    glowColor: Color = Color(0xFF00E5FF),
    isPulsing: Boolean = true,
    shapeRadius: Float = 48f
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val animatedAlpha by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1250, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(0.8f) }
    }
    
    return this.drawBehind {
        val strokeWidth = 5f
        // Draw primary blur aura
        drawRoundRect(
            color = glowColor.copy(alpha = animatedAlpha * 0.45f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius, shapeRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth * 4f)
        )
        // Draw sharp fluorescent inner core line
        drawRoundRect(
            color = glowColor.copy(alpha = 0.9f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(shapeRadius, shapeRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun HomeScreen(
    viewModel: StreamViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val configState by viewModel.streamConfig.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val logs by viewModel.systemLogs.collectAsState()
    val speed by viewModel.currentSpeed.collectAsState()
    val bitrate by viewModel.currentBitrate.collectAsState()
    val duration by viewModel.currentDuration.collectAsState()

    val config = configState ?: StreamConfig()
    var showCreditDialog by remember { mutableStateOf(false) }
    
    val currentView = LocalView.current
    DisposableEffect(isStreaming) {
        currentView.keepScreenOn = isStreaming
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    var inputUrl by remember { mutableStateOf(config.sourceVideoUrl) }
    var isPlayerMuted by remember { mutableStateOf(false) }

    // Synchronize Room state when it successfully loads
    LaunchedEffect(configState) {
        configState?.let {
            if (inputUrl != it.sourceVideoUrl) {
                inputUrl = it.sourceVideoUrl
            }
        }
    }

    // Modern ExoPlayer logic inside Composable
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    // Observe player lifecycle safely
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    exoPlayer.play()
                }
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // Auto-load and play link as soon as URL changes
    LaunchedEffect(inputUrl) {
        if (inputUrl.isNotBlank()) {
            try {
                exoPlayer.setMediaItem(MediaItem.fromUri(inputUrl))
                exoPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mute/Unmute implementation
    LaunchedEffect(isPlayerMuted) {
        exoPlayer.volume = if (isPlayerMuted) 0f else 1f
    }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF0B0F19), Color(0xFF111827), Color(0xFF030712))))
            .padding(horizontal = 20.dp)
            .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .neonGlowBorder(glowColor = Color(0xFF00E5FF).copy(alpha = 0.45f), shapeRadius = 28f)
                    .background(Color(0xFF1F2937).copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F), Color(0xFF00E5FF))), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📡", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "StreamMaster Pro ⚡",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Live restream engine (Lighting Active)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Collapsible APK Download & Update Help Guide (Bengali and English)
        item {
            var isHelpExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF00E5FF).copy(alpha = if (isHelpExpanded) 0.6f else 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isHelpExpanded) Color(0xFF1E293B) else Color(0xFF111827).copy(alpha = 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isHelpExpanded) 4.dp else 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isHelpExpanded = !isHelpExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📲", style = MaterialTheme.typography.bodyLarge)
                            }
                            Column {
                                Text(
                                    text = "এপিকে ডাউনলোড ও ইনস্টল গাইড 💾",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isHelpExpanded) "সহজ ৪টি ধাপে অ্যাপ ফোনে নিন" else "ডাউনলোড করতে সমস্যা? এখানে ক্লিক করুন!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isHelpExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Help",
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    AnimatedVisibility(
                        visible = isHelpExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFF00E5FF).copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "গুগল এআই স্টুডিও থেকে এপিকে (APK) ফাইল আপনার ফোনে নামানোর ৩টি সহজ ডিরেক্ট উপায়:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.Bold,
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Step 1
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(0xFF00E5FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("১", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0B0F19), fontWeight = FontWeight.Black)
                                }
                                Column {
                                    Text(
                                        text = "এআই স্টুডিওর সেটিংস প্যানেল থেকে (Easy Export)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "আপনার ব্রাউজার স্ক্রিনের ওপরের ডানদিকের গিয়ার (Gear/Hamburger) আইকনে ক্লিক করুন, সেখান থেকে 'Generate APK' সিলেক্ট করুন। ৩ মিনিট সময় লাগবে, তারপর ডাউনলোড লিংক স্ক্রিনে চলে আসবে।",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Step 2
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(0xFF00E5FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("২", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0B0F19), fontWeight = FontWeight.Black)
                                }
                                Column {
                                    Text(
                                        text = "সরাসরি এডমিন আপডেটেড ডাউনলোড লিংক (Direct Link)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "এডমিন কর্তৃক আপলোড করা গুগল ড্রাইভ বা অন্য সরাসরি ইনস্টলার লিংক থেকে যেকোনো সময় ডাউনলোড করতে নিচের নীল বাটনে ট্যাপ করুন।",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Step 3
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(0xFF00E5FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("৩", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0B0F19), fontWeight = FontWeight.Black)
                                }
                                Column {
                                    Text(
                                        text = "ইনস্টলেশন ওয়ার্নিং এড়িয়ে চলুন (Install Unknown Apps)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "ডাউনলোড শেষে ইন্সটল করার সময় অ্যান্ড্রয়েড 'Unknown Sources' বা 'Chrome/Browser Install' ওয়ার্নিং দেখাবে, দয়া করে 'Install Anyway' বা 'পারমিশন দিন' দিয়ে সফলভাবে ইনস্টল করে নিন।",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            
                            val localContext = LocalContext.current
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(config.appDownloadUrl.trim()))
                                        localContext.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সরাসরি লিঙ্ক থেকে এপিকে (APK) নামান ⚡",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF0B0F19)
                                )
                            }
                        }
                    }
                }
            }
        }

        // System Announcement & Credits Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neonGlowBorder(glowColor = Color(0xFF10B981).copy(alpha = 0.45f), shapeRadius = 28f)
                    .background(Color(0xFF1E2937).copy(alpha = 0.9f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title info & balance badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Add, // Standard add wallet credit icon
                                contentDescription = "Credits Balance",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "আপনার ব্যালেন্স:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${config.userCredits} Credit",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF0B0F19),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "১টি লাইভ স্ট্রিম সেশন = ${config.streamCost} ক্রেডিট।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6EE7B7),
                        fontWeight = FontWeight.Bold
                    )

                    if (config.adminAnnouncement.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF10B981).copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Announcement",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = config.adminAnnouncement,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE2E8F0),
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.15
                            )
                        }
                    }

                    // Redeem Section
                    Spacer(modifier = Modifier.height(12.dp))
                    var redeemCode by remember { mutableStateOf("") }
                    var redeemStatus by remember { mutableStateOf("") }
                    var redeemColor by remember { mutableStateOf(Color(0xFF00E5FF)) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = redeemCode,
                            onValueChange = { redeemCode = it },
                            placeholder = { Text("ক্রেডিট কোড (যেমন: FREE50)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8)) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF1E293B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF4B5563)
                            )
                        )
                        Button(
                            onClick = {
                                val code = redeemCode.trim().uppercase()
                                when (code) {
                                    "FREE50" -> {
                                        val updated = config.copy(userCredits = config.userCredits + 50)
                                        viewModel.updateConfig(updated)
                                        redeemStatus = "সফলভাবে ৫০ ক্রেডিট যুক্ত হয়েছে! 🎉"
                                        redeemColor = Color(0xFF10B981)
                                        redeemCode = ""
                                    }
                                    "BUY100" -> {
                                        val updated = config.copy(userCredits = config.userCredits + 100)
                                        viewModel.updateConfig(updated)
                                        redeemStatus = "সফলভাবে ১০০ ক্রেডিট যুক্ত হয়েছে! 💰"
                                        redeemColor = Color(0xFF10B981)
                                        redeemCode = ""
                                    }
                                    "VIP500" -> {
                                        val updated = config.copy(userCredits = config.userCredits + 500)
                                        viewModel.updateConfig(updated)
                                        redeemStatus = "সফলভাবে ৫০০ ভিআইপি ক্রেডিট যুক্ত হয়েছে! 👑"
                                        redeemColor = Color(0xFF10B981)
                                        redeemCode = ""
                                    }
                                    "ADMINPASS" -> {
                                        val updated = config.copy(isAdminOn = true, userCredits = config.userCredits + 1000)
                                        viewModel.updateConfig(updated)
                                        redeemStatus = "এডমিন অ্যাক্সেস এবং ১০০০ ক্রেডিট আনলক হয়েছে! 🛠️"
                                        redeemColor = Color(0xFF8B5CF6)
                                        redeemCode = ""
                                    }
                                    else -> {
                                        if (code.isNotBlank()) {
                                            redeemStatus = "ভুল কোড! সঠিক কোড দিন বা কিনুন ❌"
                                            redeemColor = Color.Red
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("রিডিম", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = Color(0xFF0B0F19))
                        }
                    }

                    if (redeemStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = redeemStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = redeemColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Integrated ExoPlayer Video window panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .neonGlowBorder(glowColor = if (isStreaming) Color(0xFFFF007F) else Color(0xFF00E5FF), shapeRadius = 48f)
                    .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .testTag("player_window_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1424)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (inputUrl.isNotBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Submit an active streaming URL to preview here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Bottom Floating Controllers inside Video Card
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { isPlayerMuted = !isPlayerMuted },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = if (isPlayerMuted) Icons.Default.Close else Icons.Default.Share, 
                                contentDescription = "Mute or Unmute Preview",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Top Indicators Overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isStreaming) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = pulseAlpha))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE ●",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "720p @ 60fps",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Paste Stream Link Text Input Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOURCE INPUT (HLS/RTMP/MP4)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = {
                            inputUrl = it
                            viewModel.updateConfig(config.copy(sourceVideoUrl = it))
                        },
                        placeholder = { Text("Paste MP4, HLS (.m3u8), or RTSP Link here", color = Color(0xFF94A3B8).copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("source_link_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "দ্রুত টেস্ট করতে একটি স্যাম্পল সোর্স সিলেক্ট করুন (Tap to Test):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val presets = listOf(
                            Triple("🐰 Bunny (HLS)", "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "Big Buck Bunny HLS Stream"),
                            Triple("🎬 Sintel (MP4)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4", "Sintel Movie MP4 Trailer"),
                            Triple("⚡ Tears (MP4)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4", "Tears of Steel HD Clip"),
                            Triple("🐘 Dream (HLS)", "https://playertest.longtailvideo.com/adaptive/elephants_dream/elephants_dream.m3u8", "Elephant's Dream Adaptive HLS")
                        )
                        presets.forEach { (name, url, desc) ->
                            FilterChip(
                                selected = inputUrl == url,
                                onClick = {
                                    inputUrl = url
                                    viewModel.updateConfig(config.copy(sourceVideoUrl = url))
                                },
                                label = { Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (inputUrl == url) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF1E293B),
                                    selectedContainerColor = Color(0xFF00E5FF),
                                    labelColor = Color(0xFF94A3B8),
                                    selectedLabelColor = Color(0xFF0F172A),
                                    selectedLeadingIconColor = Color(0xFF0F172A)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = inputUrl == url,
                                    borderColor = Color(0xFF475569),
                                    selectedBorderColor = Color(0xFF00E5FF)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Stream Telemetry / Targets Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neonGlowBorder(glowColor = if (isStreaming) Color(0xFFFF007F) else Color(0xFF3B82F6), shapeRadius = 24f)
                    .border(
                        width = 1.5.dp,
                        color = (if (isStreaming) Color(0xFFFF007F) else Color(0xFF3B82F6)).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.85f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Restream Targets",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isStreaming) "FFMPEG CORE ONLINE" else "FFMPEG CORE READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isStreaming) Color(0xFFFF007F) else Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            TelemetryInfoItem(
                                label = "FPS (Output)",
                                value = if (isStreaming) "30.0" else "0.0",
                                icon = Icons.Default.List,
                                iconColor = Color(0xFF00E5FF)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            TelemetryInfoItem(
                                label = "Speed Factor",
                                value = if (isStreaming) "%.2fx".format(speed) else "0.00x",
                                icon = Icons.Default.Refresh,
                                iconColor = Color(0xFF10B981)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TelemetryInfoItem(
                                label = "Output Bitrate",
                                value = if (isStreaming) "%.0f kB/s".format(bitrate) else "0 kB/s",
                                icon = Icons.Default.PlayArrow,
                                iconColor = Color(0xFF00E5FF)
                            )
                            val min = duration / 60
                            val sec = duration % 60
                            Text(
                                text = "Duration: %02d:%02d".format(min, sec),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isStreaming) {
                                viewModel.stopStreaming(context)
                            } else {
                                if (config.userCredits < config.streamCost) {
                                    showCreditDialog = true
                                } else {
                                    // Deduct credits and update config
                                    val updated = config.copy(
                                        userCredits = config.userCredits - config.streamCost
                                    )
                                    viewModel.updateConfig(updated)
                                    viewModel.startStreaming(context)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStreaming) Color(0xFFFF007F) else Color(0xFF00E5FF)
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .neonGlowBorder(glowColor = if (isStreaming) Color(0xFFFF007F) else Color(0xFF00E5FF), shapeRadius = 56f)
                            .testTag("control_stream_button")
                    ) {
                        Text(
                            text = if (isStreaming) "🛑 STOP MULTI-STREAM" else "🚀 START MULTI-STREAM",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isStreaming) Color.White else Color(0xFF0B0F19)
                        )
                    }

                    if (showCreditDialog) {
                        AlertDialog(
                            onDismissRequest = { showCreditDialog = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                                    Text("পর্যাপ্ত ক্রেডিট নেই! ⚠️", fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Text(
                                    "আপনার বর্তমান ব্যালেন্স: ${config.userCredits} ক্রেডিট এবং প্রতিটি সেশনের খরচ: ${config.streamCost} ক্রেডিট। দয়া করে ক্রেডিট রিডিম করুন বা এডমিন প্যানেল ব্যবহার করুন।"
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showCreditDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                                ) {
                                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Live scrolling terminal output component
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF0061A4), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "System Terminal Logs",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isStreaming) "STREAMING" else "WAITING",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isStreaming) Color(0xFF22C55E) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val listState = rememberLazyListState()
                    if (logs.isNotEmpty()) {
                        LaunchedEffect(logs.size) {
                            coroutineScope.launch {
                                listState.scrollToItem(logs.size - 1)
                            }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A).copy(alpha = 0.2f))
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { logLine ->
                                Text(
                                    text = logLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (logLine.contains("[Simulation Error]") || logLine.contains("Exception") || logLine.contains("failed")) Color(0xFFF87171) else Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Terminal output idle. Press START MULTI-STREAM to begin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1C1B1B),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF49454F)
        )
    }
}

@Composable
fun TelemetryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )
    }
}
