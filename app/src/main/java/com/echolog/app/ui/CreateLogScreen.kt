package com.echolog.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.viewmodel.LogViewModel

@Composable
fun CreateLogScreen(
    viewModel: LogViewModel,
    onSaveSuccess: () -> Unit
) {
    // Explicitly specifying <String> fixes the "Cannot infer type" error
    var title by remember { mutableStateOf<String>("") }
    var caption by remember { mutableStateOf<String>("") }
    var selectedCategory by remember { mutableStateOf<String>("General") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "New Log",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("What's on your mind?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Media Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /* TODO: Open Camera */ }) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
            }
            IconButton(onClick = { /* TODO: Record Voice */ }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice")
            }
            IconButton(onClick = { /* TODO: Pick Gallery */ }) {
                Icon(Icons.Default.Collections, contentDescription = "Gallery")
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                // Ensure this function exists in LogViewModel
                viewModel.saveNewLog(
                    title = title,
                    caption = caption,
                    category = selectedCategory,
                    type = "memory",
                    mediaPaths = emptyList()
                )
                onSaveSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Save to Vault", color = Color.White)
        }
    }
}