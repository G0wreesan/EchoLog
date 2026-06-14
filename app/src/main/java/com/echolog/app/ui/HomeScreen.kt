package com.echolog.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.viewmodel.LogViewModel
import com.echolog.app.viewmodel.RegistrationViewModel
import java.time.Instant
import java.time.LocalTime

@Composable
fun HomeScreen(
    viewModel: LogViewModel,
    regViewModel: RegistrationViewModel
) {
    val logs by viewModel.recentLogs.collectAsState()
    val userProfile by regViewModel.userProfile.collectAsState()

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Scaffold(containerColor = Color(0xFFF8F8F8)) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "$greeting, ${userProfile?.display_name ?: "Echo User"}",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text("EchoLog", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Your memory lane", color = Color.Gray, fontSize = 14.sp)
                }
            }

            val upcoming = logs.filter { log ->
                log.scheduledAt?.let { scheduledString ->
                    try {
                        scheduledString.toLong() > System.currentTimeMillis()
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }

            if (upcoming.isNotEmpty()) {
                item {
                    Text(
                        "UPCOMING",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                items(upcoming) { log ->
                    HomeLogCard(log = log, onClick = {
                        viewModel.navigateToLog(log.id)
                    })
                }
            }

            item {
                Text(
                    "RECENT ACTIVITY",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            if (logs.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs yet. Tap + to start.", color = Color.LightGray)
                    }
                }
            } else {
                items(logs) { log ->
                    HomeLogCard(log = log, onClick = {
                        viewModel.navigateToLog(log.id)
                    })
                }
            }
        }
    }
}