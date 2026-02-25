package com.example.activelife.ui

import androidx.compose.ui.res.painterResource
import com.example.activelife.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// --- NEW DESIGN SYSTEM COLORS ---


@Composable
fun ReportsScreen(viewModel: ActivityViewModel, navController: NavController) {
    val weeklyData by viewModel.weeklyData.collectAsState()
    val stepGoal = 10000

    // Dynamic Analytics Math
    val totalSteps = weeklyData.sumOf { it.second }
    val avgSteps = if (weeklyData.isNotEmpty()) totalSteps / weeklyData.size else 0
    val goalsMet = weeklyData.count { it.second >= stepGoal }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // --- HEADER ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardBackground,
                modifier = Modifier.size(40.dp),
                onClick = { navController.popBackStack() }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Reports", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Analytics & Progress", color = TextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- WEEKLY / MONTHLY TOGGLE ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardBackground,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = LimeAccent,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Weekly", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Monthly", color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SUMMARY STATS (Updated with Vector Icons) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), iconRes = R.drawable.ic_walk, value = "%,d".format(totalSteps), label = "Total Steps")
            StatCard(modifier = Modifier.weight(1f), iconRes = R.drawable.ic_trend, value = "%,d".format(avgSteps), label = "Avg/day")
            StatCard(modifier = Modifier.weight(1f), iconRes = R.drawable.ic_target, value = "$goalsMet/7", label = "Goals Met")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- DAILY STEPS BAR CHART ---
        BarChartSection(weeklyData)

        Spacer(modifier = Modifier.height(32.dp))

        // --- GOAL PROGRESS LIST ---
        Text("Goal Progress", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        weeklyData.forEach { (day, steps) ->
            GoalProgressRow(day = day, steps = steps, goal = stepGoal)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Updated StatCard to accept vector assets
@Composable
fun StatCard(modifier: Modifier, iconRes: Int, value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = LimeAccent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(label, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun BarChartSection(weeklyData: List<Pair<String, Int>>) {
    val maxSteps = maxOf(16000, weeklyData.maxOfOrNull { it.second } ?: 16000)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        modifier = Modifier.fillMaxWidth().height(260.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Daily Steps", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("16k", color = TextSecondary, fontSize = 10.sp)
                    Text("12k", color = TextSecondary, fontSize = 10.sp)
                    Text("8k", color = TextSecondary, fontSize = 10.sp)
                    Text("4k", color = TextSecondary, fontSize = 10.sp)
                    Text("0k", color = TextSecondary, fontSize = 10.sp)
                }

                weeklyData.reversed().forEach { (day, steps) ->
                    val progress = (steps.toFloat() / maxSteps.toFloat()).coerceIn(0f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .weight(1f, fill = false)
                                .fillMaxHeight(progress)
                                .background(LimeAccent, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(day.take(3), color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressRow(day: String, steps: Int, goal: Int) {
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(day.take(3), color = TextSecondary, fontSize = 14.sp)
            Text("%,d".format(steps), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = LimeAccent,
            trackColor = CardBackground
        )
    }
}