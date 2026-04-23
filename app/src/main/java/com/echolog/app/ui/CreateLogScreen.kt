package com.echolog.app.ui

import android.Manifest
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.echolog.app.viewmodel.LogViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogScreen(viewModel: LogViewModel, onSaveSuccess: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var scheduledAt by remember { mutableStateOf<Long?>(null) }

    val selectedMediaPaths = remember { mutableStateListOf<String>() }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Helper to generate camera URI
    fun getTempUri(): Uri {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "com.echolog.app.fileprovider", file)
    }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedMediaPaths.add(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempPhotoUri?.let { selectedMediaPaths.add(it.toString()) }
    }

    // PERMISSION CHECKER
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val canCamera = perms[Manifest.permission.CAMERA] ?: false
        val canAudio = perms[Manifest.permission.RECORD_AUDIO] ?: false

        if (!canCamera) Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("New Log", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth().height(120.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Categories
        Text("Category", fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
            listOf("General", "Personal", "Work", "Ideas").forEach { cat ->
                FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) }, modifier = Modifier.padding(end = 8.dp))
            }
        }

        // Colors
        Text("Log Color", fontWeight = FontWeight.SemiBold)
        Row(Modifier.padding(vertical = 8.dp)) {
            listOf(Color.Black, Color.Red, Color.Blue, Color.Magenta, Color.DarkGray).forEach { color ->
                Box(modifier = Modifier.size(36.dp).background(color, CircleShape).border(if (selectedColor == color) 3.dp else 0.dp, Color.LightGray, CircleShape).clickable { selectedColor = color })
                Spacer(Modifier.width(12.dp))
            }
        }

        // Media Controls
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IconButton(onClick = {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                val uri = getTempUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }) { Icon(Icons.Default.PhotoCamera, "Camera") }

            IconButton(onClick = {
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                // TODO: Start audio recording logic
            }) { Icon(Icons.Default.Mic, "Voice") }

            IconButton(onClick = { galleryLauncher.launch("image/*") }) { Icon(Icons.Default.Collections, "Gallery") }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                viewModel.saveNewLog(
                    title = title,
                    caption = caption,
                    category = selectedCategory,
                    type = "memory",
                    mediaPaths = selectedMediaPaths.toList(),
                    colorHex = String.format("#%06X", (0xFFFFFF and selectedColor.toArgb())),
                    scheduledAt = scheduledAt,
                    context = context // Passing context here!
                )
                onSaveSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
        ) {
            Text("Save to Vault", color = Color.White)
        }
    }
}