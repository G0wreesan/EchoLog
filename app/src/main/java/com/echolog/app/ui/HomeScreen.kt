package com.echolog.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.viewmodel.LogViewModel

@Composable
fun HomeScreen(viewModel: LogViewModel) {
    val logs by viewModel.recentLogs.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F8F8) // Light grey background to make white cards pop
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("EchoLog", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Your memory lane", color = Color.Gray, fontSize = 14.sp)
                }
            }

            // Section 1: Upcoming (Filtered logs)
            // Section 1: Upcoming (Filtered logs)
            val upcoming = logs.filter { log ->
                log.scheduledAt?.let { scheduledStr ->
                    try {
                        // Parse ISO-8601 string back to milliseconds
                        val scheduledMillis = java.time.Instant.parse(scheduledStr).toEpochMilli()
                        scheduledMillis > System.currentTimeMillis()
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
                    HomeLogCard(log)
                }
            }

            // Section 2: Recent Memories
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
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No logs yet. Tap + to start.", color = Color.LightGray)
                    }
                }
            } else {
                items(logs) { log ->
                    HomeLogCard(log)
                }
            }
        }
    }
}