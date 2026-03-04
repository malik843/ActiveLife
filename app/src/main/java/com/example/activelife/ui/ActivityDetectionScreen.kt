package com.example.activelife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.activelife.data.ActivityLog
import java.text.SimpleDateFormat
import java.util.*

// Local color variables so this file can compile independently


@Composable
fun ActivityDetectionScreen(viewModel: ActivityViewModel) {
    // Collect Real-Time Data (Status + History)
    val currentStatus by viewModel.currentStatus.collectAsState()
    val historyLogs by viewModel.todayLogs.collectAsState()

    // --- THIS IS THE MISSING LINE! ---
    val livePosture by viewModel.livePosture.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Live Activity",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- MAIN STATUS CARD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().height(220.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logic to choose Icon based on Live Status
                val statusData = when (livePosture) {
                    "ACTIVE" -> Triple(Icons.Default.DirectionsRun, LimeAccent, "Active")
                    else -> Triple(Icons.Default.Weekend, TextSecondary, "Still")
                }

                // Icon Circle
                Surface(
                    shape = CircleShape,
                    color = statusData.second.copy(alpha = 0.1f),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = statusData.first, contentDescription = null, tint = statusData.second, modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Text
                Text(
                    text = statusData.third,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- RECENT ACTIVITIES LIST ---
        Text(
            text = "Recent Updates",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (historyLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.TopCenter) {
                Text("No activity changes detected yet today.", color = TextSecondary, modifier = Modifier.padding(top = 32.dp))
            }
        } else {
            // Content padding added to prevent list items from hiding behind the bottom navigation bar
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(historyLogs.reversed()) { log ->
                    ActivityLogItem(log)
                }
            }
        }
    }
}

@Composable
fun ActivityLogItem(log: ActivityLog) {
    val isWalking = log.activityType == "WALKING" || log.activityType == "RUNNING"
    val icon = if (isWalking) Icons.Default.DirectionsRun else Icons.Default.Weekend
    // Apply Lime Green only when active, gray when sitting
    val color = if (isWalking) LimeAccent else TextSecondary

    val timeLabel = try {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.timestamp))
    } catch (e: Exception) { "Now" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = log.activityType, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Status Change", fontSize = 12.sp, color = TextSecondary)
            }
        }
        Text(text = timeLabel, fontWeight = FontWeight.SemiBold, color = TextSecondary, fontSize = 14.sp)
    }
}