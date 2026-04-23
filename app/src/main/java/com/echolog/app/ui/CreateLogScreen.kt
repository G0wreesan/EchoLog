package com.echolog.app.ui

import android.Manifest
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.echolog.app.viewmodel.LogViewModel
import java.io.File
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.echolog.app.util.AudioRecorder // Import your utility
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogScreen(
    viewModel: LogViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // UI State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Study", "Work", "Workout", "Personal", "Travel", "General")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expanded by remember { mutableStateOf(false) }

    // Date & Reminder State
    var scheduledAt by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Media Logic State
    val selectedMediaPaths = remember { mutableStateListOf<String>() }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Audio State
    var isRecording by remember { mutableStateOf(false) }
    val recorder = remember { AudioRecorder(context) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.RECORD_AUDIO] == false) {
            Toast.makeText(context, "Mic permission needed", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera/Gallery Launchers (Keep your existing ones)
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedMediaPaths.add(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempPhotoUri?.let { selectedMediaPaths.add(it.toString()) }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    scheduledAt = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = Color.Black) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Create Entry", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description", color = Color.Black) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reminder Button
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (scheduledAt != null) Color.Blue else Color.Gray)
        ) {
            Icon(Icons.Default.NotificationsActive, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text(if (scheduledAt == null) "Add Reminder" else "Reminder Set", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Add Media", fontWeight = FontWeight.Bold, color = Color.Black)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Camera
            IconButton(onClick = {
                val uri = FileProvider.getUriForFile(context, "com.echolog.app.fileprovider",
                    File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_${System.currentTimeMillis()}.jpg"))
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }) { Icon(Icons.Default.PhotoCamera, null, tint = Color.Black) }

            // Mic (Audio Recording)
            IconButton(
                onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                    if (!isRecording) {
                        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.mp3")
                        audioFile = file
                        recorder.startRecording(file)
                        isRecording = true
                    } else {
                        recorder.stopRecording()
                        isRecording = false
                        audioFile?.let { selectedMediaPaths.add(Uri.fromFile(it).toString()) }
                    }
                },
                modifier = Modifier.background(if (isRecording) Color.Red else Color.Transparent, CircleShape)
            ) { Icon(Icons.Default.Mic, null, tint = if (isRecording) Color.White else Color.Black) }

            // Gallery
            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.Collections, null, tint = Color.Black)
            }
        }

        // Preview Row
        if (selectedMediaPaths.isNotEmpty()) {
            LazyRow(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                items(selectedMediaPaths) { path ->
                    Box(Modifier.padding(end = 8.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(path),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(onClick = { selectedMediaPaths.remove(path) }, Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(0.6f), CircleShape)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.saveNewLog(
                    title = title,
                    caption = description,
                    category = selectedCategory,
                    type = "memory",
                    mediaPaths = selectedMediaPaths.toList(),
                    scheduledAt = scheduledAt,
                    context = context
                )
                onSaveSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Memory", fontWeight = FontWeight.Bold)
        }
    }
}