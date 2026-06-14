package com.echolog.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.echolog.app.data.LogEntity
import com.echolog.app.ui.components.HomeLogCard
import com.echolog.app.ui.components.MediaItemRow
import com.echolog.app.ui.components.MediaPreviewScreen
import com.echolog.app.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: LogViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val logs by viewModel.recentLogs.collectAsState()

    var selectedLog by remember { mutableStateOf<LogEntity?>(null) }
    var showModal by remember { mutableStateOf(false) }

    var previewMediaUrl by remember { mutableStateOf<String?>(null) }

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
                    showModal = true
                })
            }
        }
    }

    if (showModal && selectedLog != null) {
        val currentLog = selectedLog!!

        Dialog(
            onDismissRequest = { showModal = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = 560.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                val images = (currentLog.localImagePaths + currentLog.remoteImageUrls).distinct()
                val audios = (currentLog.localAudioPaths + currentLog.remoteAudioUrls).distinct()
                val videos = (currentLog.localVideoPaths + currentLog.remoteVideoUrls).distinct()
                val totalMediaCount = images.size + audios.size + videos.size

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = editedTitle,
                                onValueChange = { editedTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                        } else {
                            Text(
                                text = currentLog.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                if (isEditing) {
                                    val updatedLog = currentLog.copy(
                                        title = editedTitle,
                                        caption = editedCaption
                                    )
                                    viewModel.saveAndSyncLog(updatedLog, context)
                                    selectedLog = updatedLog
                                }
                                isEditing = !isEditing
                            }) {
                                Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = "Edit")
                            }

                            IconButton(onClick = { showModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                            }
                        }
                    }

                    Text(
                        text = "Category • ${currentLog.category}",
                        color = Color(0xCC3FC1FD),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editedCaption,
                            onValueChange = { editedCaption = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Caption") }
                        )
                    } else {
                        Text(currentLog.caption ?: "No description.", color = Color.DarkGray, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Media Files ($totalMediaCount)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (images.isNotEmpty()) {
                        Text("Photos", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                        images.forEach { path ->
                            MediaItemRow(
                                path = path,
                                isEditing = isEditing,
                                onMediaClick = { previewMediaUrl = path },
                                onDeleteMedia = {
                                    val updatedLog = currentLog.copy(
                                        localImagePaths = currentLog.localImagePaths.filter { it != path },
                                        remoteImageUrls = currentLog.remoteImageUrls.filter { it != path }
                                    )
                                    viewModel.saveAndSyncLog(updatedLog, context)
                                    selectedLog = updatedLog
                                }
                            )
                        }
                    }

                    if (audios.isNotEmpty()) {
                        Text("Voice Notes", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                        audios.forEach { path ->
                            MediaItemRow(
                                path = path,
                                isEditing = isEditing,
                                onMediaClick = { previewMediaUrl = path },
                                onDeleteMedia = {
                                    val updatedLog = currentLog.copy(
                                        localAudioPaths = currentLog.localAudioPaths.filter { it != path },
                                        remoteAudioUrls = currentLog.remoteAudioUrls.filter { it != path }
                                    )
                                    viewModel.saveAndSyncLog(updatedLog, context)
                                    selectedLog = updatedLog
                                }
                            )
                        }
                    }

                    if (videos.isNotEmpty()) {
                        Text("Videos", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                        videos.forEach { path ->
                            MediaItemRow(
                                path = path,
                                isEditing = isEditing,
                                onMediaClick = { previewMediaUrl = path },
                                onDeleteMedia = {
                                    val updatedLog = currentLog.copy(
                                        localVideoPaths = currentLog.localVideoPaths.filter { it != path },
                                        remoteVideoUrls = currentLog.remoteVideoUrls.filter { it != path }
                                    )
                                    viewModel.saveAndSyncLog(updatedLog, context)
                                    selectedLog = updatedLog
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    previewMediaUrl?.let { url ->
        MediaPreviewScreen(
            mediaPath = url,
            onDismiss = { previewMediaUrl = null }
        )
    }
}
