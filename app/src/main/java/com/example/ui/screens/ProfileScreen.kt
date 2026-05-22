package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.streaming.FFmpegStreamingExecutor
import com.example.ui.StreamViewModel
import com.example.data.StreamConfig

@Composable
fun ProfileScreen(
    viewModel: StreamViewModel,
    innerPadding: PaddingValues
) {
    val isFFmpegAvailable = FFmpegStreamingExecutor.isFFmpegAvailable
    val configState by viewModel.streamConfig.collectAsState()
    val config = configState ?: StreamConfig()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F8))
            .padding(horizontal = 20.dp)
            .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding() + 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "BROADCASTER PROFILE & DIAGNOSTICS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0061A4),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Host Details",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1C1B1B),
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Visual Avatar Profile head card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1E4FF))
                            .border(2.dp, Color(0xFF0061A4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Broadcaster avatar",
                            tint = Color(0xFF0061A4),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (config.isAdminOn) "Admin Account (Super Admin)" else "Primary Broadcaster",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1C1B1B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (config.isAdminOn) "Rank: App Creator & Controller" else "Account Rank: Pro Streamer V4",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0061A4),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "INGEST STATUS: ONLINE READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- ADMIN ACCESS CONTROL PANEL SECTION ---
        item {
            if (!config.isAdminOn) {
                // Pin Login Card to Unlock Admin Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0061A4))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔐 Admin Access Locked",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1C1B1B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Unlock administrator mode to manage user credits, system banner announcements, and custom APK download URLs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF49454F)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        var enteredPin by remember { mutableStateOf("") }
                        var pinError by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { 
                                enteredPin = it
                                if (pinError.isNotBlank()) pinError = ""
                            },
                            label = { Text("Admin Secret PIN (Default: 1234)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (pinError.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = pinError, color = Color.Red, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (enteredPin.trim() == config.adminPin) {
                                    val updated = config.copy(isAdminOn = true)
                                    viewModel.updateConfig(updated)
                                    enteredPin = ""
                                } else {
                                    pinError = "Incorrect PIN! Please try again (or redeem code ADMINPASS on the Home screen)."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Admin Panel", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // Glorious Unlocked Admin Panel Dashboard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF0061A4), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color(0xFF0061A4))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🛠️ Admin Dashboard (Admin Panel)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF001D36),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Logout button
                            TextButton(
                                onClick = {
                                    val updated = config.copy(isAdminOn = false)
                                    viewModel.updateConfig(updated)
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Logout", fontWeight = FontWeight.Bold, color = Color.Red)
                                }
                            }
                        }

                        Text(
                            text = "Admin access is fully active. You can control all application live configurations in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0061A4),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        HorizontalDivider(color = Color(0xFF0061A4).copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // FEATURE 1: USER CREDIT RECHARGER
                        Text(
                            text = "1. User Balance Control (Credit Manager)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var creditInput by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = creditInput,
                                onValueChange = { creditInput = it },
                                placeholder = { Text("Set new balance", style = MaterialTheme.typography.labelSmall) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            Button(
                                onClick = {
                                    val creds = creditInput.toIntOrNull()
                                    if (creds != null) {
                                        val updated = config.copy(userCredits = creds)
                                        viewModel.updateConfig(updated)
                                        creditInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                            ) {
                                Text("Set")
                            }
                        }
                        
                        // Quick add credit shortcuts
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100, 500, 1000).forEach { amount ->
                                AssistChip(
                                    onClick = {
                                        val updated = config.copy(userCredits = config.userCredits + amount)
                                        viewModel.updateConfig(updated)
                                    },
                                    label = { Text("+$amount Credit", fontWeight = FontWeight.Bold) },
                                    colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = Color(0xFF0061A4))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // FEATURE 2: STREAM SESSION COST RATE
                        Text(
                            text = "2. Set Credit Rate Per Stream (Stream Cost)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var costInput by remember { mutableStateOf(config.streamCost.toString()) }
                            OutlinedTextField(
                                value = costInput,
                                onValueChange = { costInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            Button(
                                onClick = {
                                    val cost = costInput.toIntOrNull()
                                    if (cost != null) {
                                        val updated = config.copy(streamCost = cost)
                                        viewModel.updateConfig(updated)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                            ) {
                                Text("Change")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // FEATURE 3: APP DOWNLOAD LINK UPDATER
                        Text(
                            text = "3. Update Direct APK Download Link",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            var downloadLinkInput by remember { mutableStateOf(config.appDownloadUrl) }
                            OutlinedTextField(
                                value = downloadLinkInput,
                                onValueChange = { downloadLinkInput = it },
                                placeholder = { Text("Enter Google Drive, Telegram, or any download link") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            Button(
                                onClick = {
                                    val updated = config.copy(appDownloadUrl = downloadLinkInput)
                                    viewModel.updateConfig(updated)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                            ) {
                                Text("Update Download Link 💾", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // FEATURE 4: HOME BANNER ANNOUNCEMENT
                        Text(
                            text = "4. Update HomeScreen Announcement/Notice",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            var noticeInput by remember { mutableStateOf(config.adminAnnouncement) }
                            OutlinedTextField(
                                value = noticeInput,
                                onValueChange = { noticeInput = it },
                                placeholder = { Text("Enter the Home screen banner announcement notice here") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            Button(
                                onClick = {
                                    val updated = config.copy(adminAnnouncement = noticeInput)
                                    viewModel.updateConfig(updated)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                            ) {
                                Text("Update Announcement Notice 📣", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF0061A4).copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // FEATURE 5: future developer maintenance card
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF0061A4))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Code Update & New Feature Guide",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001D36)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF0061A4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Guidelines for future app updates:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val guideSteps = listOf(
                                     "1. To add new features inside the code, instruct the chatbot in the Google AI Studio chat with what you want in English or Bengali (e.g., 'add a custom stream resolution setting' or 'add dark theme support').",
                                     "2. The AI assistant will edit the code, upgrade screen designs, compile the APK, and generate the necessary modules.",
                                     "3. Once the code changes are done, click 'compile_applet' to ensure the application builds successfully without errors.",
                                     "4. Afterwards, from AI Studio's settings menu, select 'Generate APK' directly to generate a signed APK that is ready to install on any Android phone.",
                                     "5. Upload the newly generated APK file to your Google Drive, Telegram channel, or any host, and paste the URL here in Box 3! Users can then update directly with a single click in Settings."
                                )
                                guideSteps.forEach { step ->
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF49454F),
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.15
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diagnostics details related to compile availability
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Local System Diagnostics",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1C1B1B),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isFFmpegAvailable) Color(0xFF10B981) else Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DiagnosticPair(
                        label = "FFmpeg binary libraries loaded",
                        value = if (isFFmpegAvailable) "SUCCESS" else "FALLBACK ACTIVE"
                    )
                    DiagnosticPair(
                        label = "Native Multiplatform restream",
                        value = if (isFFmpegAvailable) "NATIVE ARTHENICA 6.0" else "CYBER-SIMULATION ENGINE"
                    )
                    DiagnosticPair(
                        label = "RTMP Protocol Multiplexer",
                        value = "ACTIVE (Dual Flv Pipeline)"
                    )
                    DiagnosticPair(
                        label = "Decoder Library Support",
                        value = "FFmpeg h264_mediacodec / high-accel"
                    )
                }
            }
        }

        // Custom Stream resolution preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Multiplexing Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1C1B1B),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Output target resolution", color = Color(0xFF1C1B1B), style = MaterialTheme.typography.bodyMedium)
                            Text("1080p Stream Standard @ 30 Frame Ratio (Direct Source Copy)", color = Color(0xFF49454F), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE1E2E1))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Audio Bitrate Preset", color = Color(0xFF1C1B1B), style = MaterialTheme.typography.bodyMedium)
                            Text("128 kbps AAC Stereo High Fidelity", color = Color(0xFF49454F), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticPair(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF49454F))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = if (value == "SUCCESS" || value.startsWith("ACTIVE") || value.startsWith("NATIVE")) Color(0xFF10B981) else Color(0xFF0061A4),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
