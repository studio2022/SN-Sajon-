package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.StreamHistory
import com.example.ui.StreamViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: StreamViewModel,
    innerPadding: PaddingValues
) {
    val history by viewModel.streamHistory.collectAsState()

    val totalStreams = history.size
    val totalTimeSeconds = history.sumOf { it.durationSeconds }
    val successCount = history.count { it.isSuccessful }
    val successRate = if (totalStreams > 0) (successCount.toFloat() / totalStreams * 100).toInt() else 100

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
                    text = "HISTORICAL METRICS & TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0061A4),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF1C1B1B),
                        fontWeight = FontWeight.Black
                    )

                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626)),
                            modifier = Modifier.testTag("clear_logs_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear History", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stats grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total sessions
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Broadcasts",
                    value = "$totalStreams",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF0061A4)
                )

                // Success rate
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Success Ratio",
                    value = "$successRate%",
                    icon = Icons.Default.Check,
                    color = Color(0xFF10B981)
                )
            }
        }

        item {
            val totalMin = totalTimeSeconds / 60
            val totalSec = totalTimeSeconds % 60
            MetricCard(
                modifier = Modifier.fillMaxWidth(),
                label = "Aggregated Stream Time",
                value = "%02d Min %02d Sec".format(totalMin, totalSec),
                icon = Icons.Default.Refresh,
                color = Color(0xFF0061A4)
            )
        }

        // Live sessions timeline title
        item {
            Text(
                text = "Transmission Session History",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1C1B1B),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF49454F), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No history recorded yet.",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFF1C1B1B),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log recordings start as soon as you output a live stream.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }
        } else {
            items(history) { session ->
                HistoryRowCard(session)
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFE1E2E1), RoundedCornerShape(24.dp)),
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
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF49454F))
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1C1B1B),
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun HistoryRowCard(session: StreamHistory) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }
    val displayDate = formatter.format(Date(session.startTime))

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
                    text = displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0061A4),
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (session.isSuccessful) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (session.isSuccessful) "Completed" else "Failed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (session.isSuccessful) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Source Link:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF49454F),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = session.sourceUrl,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1C1B1B),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Destinations Ingest:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF49454F),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = session.rtmpOutputs,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF49454F),
                maxLines = 1
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE1E2E1))

            val durationMin = session.durationSeconds / 60
            val durationSec = session.durationSeconds % 60
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Stream Duration:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF49454F)
                )
                Text(
                    text = "%02d:%02d".format(durationMin, durationSec),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1C1B1B),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
