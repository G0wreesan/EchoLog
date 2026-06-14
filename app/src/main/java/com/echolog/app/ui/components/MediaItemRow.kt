package com.echolog.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun MediaItemRow(
    path: String,
    isEditing: Boolean,
    onMediaClick: () -> Unit,
    onDeleteMedia: () -> Unit
) {
    // Robust checking for types from local names or bucket extensions
    val isVideo = path.contains("VID", ignoreCase = true) || path.endsWith(".mp4", ignoreCase = true) || path.endsWith(".mov", ignoreCase = true)
    val isAudio = path.contains("VOICE", ignoreCase = true) || path.endsWith(".mp3", ignoreCase = true) || path.endsWith(".m4a", ignoreCase = true) || path.endsWith(".wav", ignoreCase = true)

    Box(modifier = Modifier.padding(vertical = 4.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMediaClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isVideo -> {
                        Icon(Icons.Default.PlayArrow, "Video", modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Video File", modifier = Modifier.weight(1f))
                    }
                    isAudio -> {
                        Icon(Icons.Default.Mic, "Audio", modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Voice Note", modifier = Modifier.weight(1f))
                    }
                    else -> {
                        // FIX: Detect if file is local or web hosted
                        val isLocalFile = !path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("content://")

                        val imageModel = if (isLocalFile) {
                            File(path) // Force Coil to read from absolute local storage sandbox safely
                        } else {
                            path // Remote URL address string
                        }

                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Image Thumbnail",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop // Keeps image grids square without distorting aspect ratios
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Photo", modifier = Modifier.weight(1f))
                    }
                }

                if (isEditing) {
                    IconButton(onClick = onDeleteMedia) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red)
                    }
                } else {
                    // Clicking Row triggers onMediaClick preview. Clicking icon triggers primary playback execution.
                    IconButton(onClick = { onMediaClick() }) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = "Play", tint = Color(0xCC3FC1FD))
                    }
                }
            }
        }
    }
}