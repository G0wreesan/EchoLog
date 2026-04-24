package com.echolog.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MediaItemRow(path: String, isEditing: Boolean, onDeleteMedia: () -> Unit) {
    val isVideo = path.contains("video", true) || path.endsWith(".mp4")
    val isAudio = path.contains("voice", true) || path.endsWith(".mp3")

    Box(modifier = Modifier.padding(vertical = 4.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                when {
                    isVideo -> {
                        Icon(Icons.Default.PlayArrow, "Video", modifier = Modifier.size(40.dp))
                        Text("Video File", modifier = Modifier.weight(1f))
                    }
                    isAudio -> {
                        Icon(Icons.Default.Mic, "Audio", modifier = Modifier.size(40.dp))
                        Text("Voice Note", modifier = Modifier.weight(1f))
                    }
                    else -> {
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
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
                    // Play Button
                    IconButton(onClick = { /* Trigger Player Activity/Fragment */ }) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = "Play", tint = Color(0xCC3FC1FD))
                    }
                }
            }
        }
    }
}