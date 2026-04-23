package com.echolog.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Covers Delete, Search, PlayArrow, etc.
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.echolog.app.data.LogEntity
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.viewmodel.LogViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: LogViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val logs by viewModel.recentLogs.collectAsState()

    var selectedLog by remember { mutableStateOf<LogEntity?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val filteredLogs = logs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title or category...") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredLogs) { log ->
                HomeLogCard(log = log, onClick = {
                    selectedLog = log
                    showSheet = true
                })
            }
        }
    }

    if (showSheet && selectedLog != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(selectedLog!!.title, style = MaterialTheme.typography.headlineSmall)
                Text("Category: ${selectedLog!!.category}", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(selectedLog!!.caption ?: "No description available.")

                Spacer(modifier = Modifier.height(24.dp))

                // MEDIA SECTION
                selectedLog!!.localMediaPaths.forEach { path ->
                    val isVideo = path.contains("video", ignoreCase = true) || path.endsWith(".mp4")
                    val isAudio = path.contains("voice", ignoreCase = true) || path.contains("VOICE") || path.endsWith(".mp3")

                    if (isVideo) {
                        Button(onClick = { /* Intent to play video */ }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Icon(Icons.Default.PlayCircle, null) // Use PlayCircle from filled icons
                            Spacer(Modifier.width(8.dp))
                            Text("Play Video")
                        }
                    } else if (isAudio) {
                        Button(onClick = { /* Play audio */ }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Mic, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Play Voice Note")
                        }
                    } else {
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = { showSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                    Text("Delete Permanently", color = Color.Red)
                }
            }
        }
    }
}