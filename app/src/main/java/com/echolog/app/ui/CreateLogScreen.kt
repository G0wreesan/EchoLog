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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.compose.rememberAsyncImagePainter
import com.echolog.app.util.AudioRecorder
import com.echolog.app.viewmodel.LogViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogScreen(
    viewModel: LogViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current

    // ===== UI State =====
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Category Logic
    val userCategories by viewModel.userCategories.collectAsState()
    var selectedCategory by remember { mutableStateOf<String>(userCategories.firstOrNull() ?: "General") }
    var expanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    // Date/Reminder State
    var scheduledAt by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Media State
    val selectedMediaPaths = remember { mutableStateListOf<String>() }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    val recorder = remember { AudioRecorder(context) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    // ===== Launchers (Order Matters!) =====

    // 1. Camera Launcher (Defined first)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) tempPhotoUri?.let { selectedMediaPaths.add(it.toString()) }
    }

    // 2. Permission Launcher (Can now reference cameraLauncher)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val cameraGranted = perms[Manifest.permission.CAMERA] == true
        val micGranted = perms[Manifest.permission.RECORD_AUDIO] == true

        if (cameraGranted) {
            // Safe to create file and launch camera here
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else if (perms.containsKey(Manifest.permission.CAMERA)) {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { selectedMediaPaths.add(it.toString()) } }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { selectedMediaPaths.add(it.toString()) } }

    // ===== Dialogs =====
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

    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Create New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    label = { Text("Category Name") }

                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newCategoryInput.isNotBlank()) {
                        viewModel.addNewCategory(newCategoryInput)
                        selectedCategory = newCategoryInput
                        newCategoryInput = ""
                        showNewCategoryDialog = false
                    }
                }) { Text("Add") }
            }
        )
    }

    // ===== Main UI =====
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "Create Entry",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xCC3FC1FD)
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xCC3FC1FD))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xCC3FC1FD))
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                userCategories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = { selectedCategory = cat; expanded = false }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("+ Add Custom", color = Color(0xCC3FC1FD)) },
                    onClick = { expanded = false; showNewCategoryDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xCC3FC1FD))
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Text("Add Media", fontWeight = FontWeight.Bold,color = Color(0xCC3FC1FD))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // FIXED CAMERA BUTTON
            IconButton(onClick = {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }) {
                Icon(Icons.Default.PhotoCamera, null, tint = Color.Black)
            }

            IconButton(
                onClick = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                    if (!isRecording) {
                        val file = File(context.cacheDir, "VOICE_${System.currentTimeMillis()}.mp3")
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

            IconButton(onClick = { videoLauncher.launch("video/*") }) {
                Icon(Icons.Default.VideoCall, null, tint = Color.Black)
            }

            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.Collections, null, tint = Color.Black)
            }
        }

        if (selectedMediaPaths.isNotEmpty()) {
            LazyRow(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                items(selectedMediaPaths) { path ->
                    val isVideo = path.contains("video") || path.endsWith(".mp4")
                    val isAudio = path.contains("VOICE") || path.endsWith(".mp3")

                    Box(Modifier.padding(end = 12.dp).size(100.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F5F5),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            when {
                                isAudio -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.Mic, "Audio", modifier = Modifier.size(32.dp))
                                        Text("Audio", fontSize = 10.sp)
                                    }
                                }
                                isVideo -> {
                                    Box(contentAlignment = Alignment.Center) {
                                        // You'd ideally use a thumbnail loader here
                                        Icon(Icons.Default.PlayCircle, "Video", modifier = Modifier.size(32.dp))
                                    }
                                }
                                else -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(path),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        // Remove Button
                        IconButton(
                            onClick = { selectedMediaPaths.remove(path) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).offset(x = 8.dp, y = (-8).dp)
                                .background(Color.Black, CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC3FC1FD)),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Save Entry", fontWeight = FontWeight.Bold) }
    }
}