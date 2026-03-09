package com.example.activelife.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.activelife.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun DashboardScreen(viewModel: ActivityViewModel, navController: NavController) {
    // Live Data from ViewModel
    val steps by viewModel.steps.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val sittingTime by viewModel.sittingTimeFormatted.collectAsState()
    // This grabs today, and creates a list of 7 days (3 days ago to 3 days from now)
    val today = LocalDate.now()
    val weekDates = (-3..3).map { today.plusDays(it.toLong()) }



    val stepGoal = 10000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        DashboardTopBar(navController)
        Spacer(modifier = Modifier.height(32.dp))

        DateSelector()
        Spacer(modifier = Modifier.height(40.dp))

        CircularStepProgress(steps = steps, goal = stepGoal)
        Spacer(modifier = Modifier.height(40.dp))

//        StreakCard()
//        Spacer(modifier = Modifier.height(24.dp))

        MetricsRow(calories = calories, distance = distance, sittingTime = sittingTime)
        Spacer(modifier = Modifier.height(24.dp))

//        ReminderNudge()
//        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DashboardTopBar(navController: NavController) {
    // Local colors so we don't cause any conflicts!
    val cardBackground = Color(0xFF262626)
    val textSecondary = Color(0xFF969696)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // <-- This pushes the profile left and the bell right!
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LEFT SIDE: Profile Picture & Text ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.profile_avatar),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, cardBackground, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Hello, ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wave),
                        contentDescription = "Wave",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(text = "Sodiq", color = Color.White, fontSize = 16.sp) // Updated to match your screenshot!
            }
        }

        // --- RIGHT SIDE: Clickable Notification Bell ---
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clickable { navController.navigate("notifications") },
            shape = CircleShape,
            color = cardBackground
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.NotificationsNone, contentDescription = "Alerts", tint = textSecondary)
                // Red Notification Dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
            }
        }
    }
}
@Composable
fun DateSelector(todayDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))) {
    Column {
        // --- TOP HEADER (Unchanged, already perfect) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = todayDate,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                Surface(shape = CircleShape, color = CardBackground, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(shape = CircleShape, color = CardBackground, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- DYNAMIC 7-DAY CALENDAR ---
        val today = LocalDate.now()
        // Generate a list of 7 days: 3 days ago, today, and 3 days into the future
        val weekDates = (-3..3).map { today.plusDays(it.toLong()) }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Loop through our dynamic dates instead of the hardcoded list
            items(weekDates.size) { index ->
                val currentDate = weekDates[index]
                val isActive = currentDate == today // Automatically highlights today's date!

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isActive) LimeAccent else CardBackground,
                    modifier = Modifier.width(64.dp).height(85.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dynamically format the number and the day name
                        val dayNumber = currentDate.format(DateTimeFormatter.ofPattern("dd"))
                        val dayName = currentDate.format(DateTimeFormatter.ofPattern("EEE"))

                        Text(
                            text = dayNumber,
                            color = if (isActive) DarkBackground else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayName,
                            color = if (isActive) DarkBackground else TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircularStepProgress(steps: Int, goal: Int) {
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 18.dp.toPx()
            drawArc(
                color = CardBackground,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = LimeAccent,
                startAngle = 140f,
                sweepAngle = 260f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // New Footprints Vector
            Icon(
                painter = painterResource(id = R.drawable.ic_walk),
                contentDescription = "Steps Icon",
                tint = LimeAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Steps Today", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "%,d".format(steps),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LimeAccent.copy(alpha = 0.5f)) // <-- Exactly 2 arguments here
            ) {
                Text(
                    text = "Goal: %,d".format(goal),
                    color = LimeAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

//@Composable
//fun StreakCard() {
//    Surface(
//        shape = RoundedCornerShape(16.dp),
//        color = CardBackground,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_fire),
//                contentDescription = "Streak",
//                tint = Color(0xFFFF9800), // Fire Orange
//                modifier = Modifier.size(20.dp)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(
//                text = "5-day movement streak",
//                color = Color.White,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }
//    }
//}


@Composable
fun MetricsRow(calories: Int, distance: Double, sittingTime: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {



        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Weekend,
            iconTint = LimeAccent,
            title = "Sitting Time",
            value = sittingTime, // <-- NO QUOTES! Just pass the live variable here
            subtext = "Active Burned out", // Feel free to change this text too if you want!
            hasProgressBar = true
        )

        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            iconTint = LimeAccent,
            title = "Calories",
            value = "$calories Kcal",
            subtext = "Active Burned out"
        )

        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Place,
            iconTint = LimeAccent,
            title = "KILOMETERS",
            value = String.format("%.1f", distance),
            subtext = "Active Burned out"
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtext: String,
    hasProgressBar: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                if (hasProgressBar) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.6f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = LimeAccent,
                        trackColor = DarkBackground
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = subtext, color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

