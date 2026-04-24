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
import com.echolog.app.ui.components.MediaItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: LogViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val logs by viewModel.recentLogs.collectAsState()

    var selectedLog by remember { mutableStateOf<LogEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    // Edit States
    var isEditing by remember { mutableStateOf(false) }
    var editedCaption by remember { mutableStateOf("") }
    var editedTitle by remember { mutableStateOf("") }

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
        Text("Browse Entry", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xCC3FC1FD))
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search memories...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xCC3FC1FD))
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredLogs) { log ->
                HomeLogCard(log = log, onClick = {
                    selectedLog = log
                    editedCaption = log.caption ?: ""
                    editedTitle = log.title
                    isEditing = false
                    showSheet = true
                })
            }
        }
    }

    if (showSheet && selectedLog != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            // MERGE: Show both unsynced local paths and synced cloud URLs
            val allMedia = (selectedLog!!.localMediaPaths + selectedLog!!.remoteMediaUrls).distinct()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // --- TOP BAR ---
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            label = { Text("Title") }
                        )
                    } else {
                        Text(selectedLog!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = {
                        if (isEditing) {
                            val updatedLog = selectedLog!!.copy(
                                title = editedTitle,
                                caption = editedCaption
                            )
                            viewModel.saveAndSyncLog(updatedLog, context)
                        }
                        isEditing = !isEditing
                    }) {
                        Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null)
                    }
                }

                Text("Category • ${selectedLog!!.category}", color = Color(0xCC3FC1FD), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = editedCaption,
                        onValueChange = { editedCaption = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Caption") }
                    )
                } else {
                    Text(selectedLog!!.caption ?: "No description.", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- MEDIA SECTION ---
                Text("Media Files (${allMedia.size})", style = MaterialTheme.typography.titleSmall)

                allMedia.forEach { path ->
                    MediaItemRow(
                        path = path,
                        isEditing = isEditing,
                        onDeleteMedia = {
                            // Filter the deleted item out of BOTH potential lists
                            val newLocal = selectedLog!!.localMediaPaths.filter { it != path }
                            val newRemote = selectedLog!!.remoteMediaUrls.filter { it != path }
                            selectedLog = selectedLog!!.copy(
                                localMediaPaths = newLocal,
                                remoteMediaUrls = newRemote
                            )
                        }
                    )
                }

                if (isEditing) {
                    Button(
                        onClick = { /* Launch Camera/Gallery */ },
                        modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC3FC1FD))
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Media")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}