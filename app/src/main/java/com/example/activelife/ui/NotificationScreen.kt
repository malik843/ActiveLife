package com.example.activelife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // Fixes the State error
import androidx.compose.runtime.getValue      // Fixes the State error
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(navController: NavController, viewModel: ActivityViewModel) {
    // Safely scoped colors to prevent conflicts
    val darkBackground = Color(0xFF1A1A1A)
    val cardBackground = Color(0xFF262626)
    val limeAccent = Color(0xFFC8FF00)
    val textSecondary = Color(0xFF969696)

    // Read real data directly from your Room Database!
    val realNotifications by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // --- HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = cardBackground,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { navController.popBackStack() }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Alerts", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Recent nudges & updates", color = textSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- NOTIFICATIONS LIST ---
        if (realNotifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.TopCenter) {
                Text("No alerts yet! Check back later.", color = textSecondary, modifier = Modifier.padding(top = 32.dp))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(realNotifications) { notification ->

                    // Format the raw database timestamp into a beautiful readable string
                    val timeString = try {
                        SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(notification.timestamp))
                    } catch (e: Exception) { "Just now" }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Icon matching the nudge
                            Surface(
                                shape = CircleShape,
                                color = if (notification.isWarning) limeAccent.copy(alpha = 0.1f) else cardBackground,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (notification.isWarning) Icons.Default.AirlineSeatReclineNormal else Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = if (notification.isWarning) limeAccent else Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Text Content
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notification.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(notification.message, color = textSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Replaced the mock `time` with our new formatted `timeString`
                                Text(timeString, color = limeAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}