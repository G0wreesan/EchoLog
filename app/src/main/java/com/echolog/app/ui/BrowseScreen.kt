package com.echolog.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.echolog.app.data.LogEntity
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: LogViewModel) {

    var searchQuery by remember { mutableStateOf("") }
    val logs by viewModel.recentLogs.collectAsState()

    var selectedLog by remember { mutableStateOf<LogEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    val filteredLogs = logs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            "Browse Entry",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xCC3FC1FD)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // ===== SEARCH BAR =====
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search memories...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xCC3FC1FD)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ===== LIST =====
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredLogs) { log ->
                HomeLogCard(
                    log = log,
                    onClick = {
                        selectedLog = log
                        showSheet = true
                    }
                )
            }
        }
    }

    // ===== MODERN BOTTOM SHEET =====
    if (showSheet && selectedLog != null) {

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {

            val log = selectedLog!!

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                // ===== HEADER =====
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    text = "Category • ${log.category}",
                    color = Color(0xCC3FC1FD),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = log.caption ?: "No description available.",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ===== MEDIA =====
                log.localMediaPaths.forEach { path ->

                    val isVideo =
                        path.contains("video", true) || path.endsWith(".mp4")

                    val isAudio =
                        path.contains("voice", true) ||
                                path.contains("VOICE", true) ||
                                path.endsWith(".mp3")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {

                        when {
                            isVideo -> {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Play Video")
                                }
                            }

                            isAudio -> {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Mic, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Play Voice Note")
                                }
                            }

                            else -> {
                                AsyncImage(
                                    model = path,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===== DELETE ACTION =====
                TextButton(
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete Permanently", color = Color.Red)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}