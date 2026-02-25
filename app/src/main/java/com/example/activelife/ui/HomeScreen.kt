package com.example.activelife.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ActivityViewModel) {
    // 1. Observe the Live Data from the ViewModel
    val currentStatus by viewModel.currentStatus.collectAsState()
    val steps by viewModel.steps.collectAsState()


    // 2. Permission Launcher (Required for Android 10+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // If permission granted, start the service
        if (permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true) {
            viewModel.startMonitoring()
        }
    }

    // 3. The UI Layout
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text("ActiveLife Tracker") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status Card
            Text(text = "Current Activity", fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$steps",
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Start Button
            Button(
                onClick = {
                    // Check API version for permissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
                        )
                    } else {
                        viewModel.startMonitoring()
                    }
                },
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Text("START TRACKING")
            }
        }
    }
}