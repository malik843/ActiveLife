package com.example.activelife.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// --- NEW DESIGN SYSTEM COLORS ---
val DarkBackground = Color(0xFF1A1A1A)
val CardBackground = Color(0xFF262626)
val LimeAccent = Color(0xFFC8FF00)
val TextSecondary = Color(0xFF969696)

@Composable
fun MainScreen(viewModel: ActivityViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = DarkBackground // Applies the new dark theme globally
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "dashboard") {
                composable("dashboard") {
                    // We will build the new Dashboard in Phase 2
                    DashboardScreen(viewModel,navController)
                }
                composable("activity") {
                    ActivityDetectionScreen(viewModel)
                }
                composable("reports") {
                    // Placeholder for new Reports screen
                    ReportsScreen(viewModel, navController)
                }
                composable("profile") {
                    Text("Profile Screen", color = Color.White)
                }
                composable("notifications") {
                    NotificationsScreen(navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Home", "dashboard", Icons.Default.Home),
        BottomNavItem("Reports", "reports", Icons.Default.BarChart),
        BottomNavItem("Reminders", "activity", Icons.Default.EventNote),
        BottomNavItem("Profile", "profile", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = DarkBackground,
        contentColor = TextSecondary,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkBackground, // Dark icon on Lime pill
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = LimeAccent,
                    unselectedTextColor = TextSecondary,
                    // The lime pill background for the active icon
                    indicatorColor = LimeAccent
                )
            )
        }
    }
}

data class BottomNavItem(val label: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)