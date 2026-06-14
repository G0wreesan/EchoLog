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
import com.echolog.app.util.FileStorageHelper
import com.echolog.app.viewmodel.LogViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogScreen(
    viewModel: LogViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val userCategories by viewModel.userCategories.collectAsState()
    var selectedCategory by remember { mutableStateOf(userCategories.firstOrNull() ?: "General") }
    var expanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    val preSelectedDate = viewModel.selectedCalendarDate.collectAsState().value
    val initialTimestamp = preSelectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    var scheduledAt by remember {
        mutableStateOf<Long?>(if (initialTimestamp > System.currentTimeMillis()) initialTimestamp else null)
    }

    // STATE ARRAYS FOR SEPARATE MEDIA CHANNELS
    val selectedImages = remember { mutableStateListOf<String>() }
    val selectedAudios = remember { mutableStateListOf<String>() }
    val selectedVideos = remember { mutableStateListOf<String>() }

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    val recorder = remember { AudioRecorder(context) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.saveFinished.collect { finished ->
            if (finished) {
                onSaveSuccess()
            }
        }
    }

    // Camera Callback Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            try {
                // Define your permanent location inside filesDir/media
                val targetFolder = File(context.filesDir, "media").apply { mkdirs() }
                val permanentFile = File(targetFolder, "IMG_${System.currentTimeMillis()}.jpg")

                // Stream the bytes from external storage directly into permanent internal storage
                tempPhotoFile?.inputStream()?.use { input ->
                    permanentFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Add the absolute path to your state array
                selectedImages.add(permanentFile.absolutePath)

                // Clean up the temporary external file so it doesn't waste user device storage
                if (tempPhotoFile!!.exists()) {
                    tempPhotoFile!!.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback gracefully to external storage location if internal copy fails
                selectedImages.add(tempPhotoFile!!.absolutePath)
            }
        }
    }

    // Combined Permission Launcher for both Camera and Audio Notes
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val cameraGranted = perms[Manifest.permission.CAMERA] == true
        val audioGranted = perms[Manifest.permission.RECORD_AUDIO] == true

        if (cameraGranted) {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempPhotoFile = file
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else if (audioGranted) {
            // Permission for audio was granted from the button click stream
            val targetDir = File(context.filesDir, "media").apply { mkdirs() }
            val file = File(targetDir, "VOICE_${System.currentTimeMillis()}.m4a")
            audioFile = file
            recorder.startRecording(file)
            isRecording = true
        } else {
            Toast.makeText(context, "Required feature permissions were denied", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val permanentPath = FileStorageHelper.saveFileToInternalStorage(context, it.toString(), "IMG", "jpg")
            selectedImages.add(permanentPath)
        }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val permanentPath = FileStorageHelper.saveFileToInternalStorage(context, it.toString(), "VID", "mp4")
            selectedVideos.add(permanentPath)
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Create Entry", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xCC3FC1FD))
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
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
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

        Text("Add Media", fontWeight = FontWeight.Bold, color = Color(0xCC3FC1FD))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                // FIXED: Safety check permissions framework before opening the camera
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }) { Icon(Icons.Default.PhotoCamera, null, tint = Color.Black) }

            IconButton(
                onClick = {
                    if (!isRecording) {
                        // FIXED: Handled with internal callback loop architecture to stop sudden app runtime execution crashes
                        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                    } else {
                        recorder.stopRecording()
                        isRecording = false
                        audioFile?.let { selectedAudios.add(it.absolutePath) }
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

        val allDraftMedia = selectedImages + selectedAudios + selectedVideos

        if (allDraftMedia.isNotEmpty()) {
            LazyRow(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                items(allDraftMedia) { path ->
                    val isVideo = selectedVideos.contains(path)
                    val isAudio = selectedAudios.contains(path)

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
                                        Text("Audio Note", fontSize = 10.sp)
                                    }
                                }
                                isVideo -> {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PlayCircle, "Video", modifier = Modifier.size(32.dp))
                                    }
                                }
                                else -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(File(path)),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                selectedImages.remove(path)
                                selectedAudios.remove(path)
                                selectedVideos.remove(path)
                            },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).offset(x = 8.dp, y = (-8).dp).background(Color.Black, CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // Safety: If still recording, stop and save the track before final log commit
                if (isRecording) {
                    recorder.stopRecording()
                    isRecording = false
                    audioFile?.let { selectedAudios.add(it.absolutePath) }
                }

                viewModel.saveNewLogCategorized(
                    title = title,
                    caption = description,
                    category = selectedCategory,
                    imagePaths = selectedImages.toList(),
                    audioPaths = selectedAudios.toList(),
                    videoPaths = selectedVideos.toList(),
                    scheduledAt = scheduledAt,
                    context = context
                )
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xCC3FC1FD)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Save Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}