package com.echolog.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewScreen(
    mediaPath: String,
    onDismiss: () -> Unit
) {
    // Robust checks matching both local prefixes and web storage extensions
    val isVideo = mediaPath.contains("VID", ignoreCase = true) || mediaPath.endsWith(".mp4", ignoreCase = true) || mediaPath.endsWith(".mov", ignoreCase = true)
    val isAudio = mediaPath.contains("VOICE", ignoreCase = true) || mediaPath.endsWith(".mp3", ignoreCase = true) || mediaPath.endsWith(".m4a", ignoreCase = true) || mediaPath.endsWith(".wav", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Media Preview", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF121212)), // Dark theater background
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. IMAGE PREVIEW
                !isVideo && !isAudio -> {
                    // FIX: Parse model safely if it points to a local folder or web link
                    val isLocalFile = !mediaPath.startsWith("http://") && !mediaPath.startsWith("https://") && !mediaPath.startsWith("content://")
                    val imageModel = if (isLocalFile) File(mediaPath) else mediaPath

                    AsyncImage(
                        model = imageModel,
                        contentDescription = "Full Image Preview",
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        contentScale = ContentScale.Fit
                    )
                }

                // 2. VIDEO PREVIEW CONTAINER
                isVideo -> {
                    val context = LocalContext.current
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color(0xCC3FC1FD),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Video Preview",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            mediaPath.substringAfterLast("/"),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        val uri = if (mediaPath.startsWith("http")) {
                                            android.net.Uri.parse(mediaPath)
                                        } else {
                                            androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                java.io.File(mediaPath)
                                            )
                                        }
                                        setDataAndType(uri, "video/*")
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "No app found to play video", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC3FC1FD))
                        ) {
                            Text("Open in Video Player", color = Color.Black)
                        }
                    }
                }

                // 3. AUDIO PREVIEW CONTAINER
                isAudio -> {
                    val context = LocalContext.current
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Audio Playback",
                                tint = Color(0xCC3FC1FD),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Voice Note", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                mediaPath.substringAfterLast("/"),
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            val uri = if (mediaPath.startsWith("http")) {
                                                android.net.Uri.parse(mediaPath)
                                            } else {
                                                androidx.core.content.FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    java.io.File(mediaPath)
                                                )
                                            }
                                            setDataAndType(uri, "audio/*")
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No app found to play audio", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC3FC1FD))
                            ) {
                                Text("Play Audio", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}