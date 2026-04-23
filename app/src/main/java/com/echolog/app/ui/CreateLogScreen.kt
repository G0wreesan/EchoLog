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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogScreen(
    viewModel: LogViewModel,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current

    // UI State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Study", "Work", "Workout", "Personal", "Travel", "General")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expanded by remember { mutableStateOf(false) }

    // Media Logic State
    val selectedMediaPaths = remember { mutableStateListOf<String>() }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // --- FUNCTIONAL LOGIC (From Your Code) ---
    fun getTempUri(): Uri {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "com.echolog.app.fileprovider", file)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedMediaPaths.add(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempPhotoUri?.let { selectedMediaPaths.add(it.toString()) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.CAMERA] == false) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // --- UI LAYOUT (Friend's Style + Merged Features) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(text = "Create Entry", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown from friend's code
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { selectedCategory = category; expanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Combined Media Section
        Text(text = "Add Media", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                val uri = getTempUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }) { Icon(Icons.Default.PhotoCamera, "Camera") }

            IconButton(onClick = {
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                // Audio Logic goes here
            }) { Icon(Icons.Default.Mic, "Voice") }

            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                Icon(Icons.Default.Collections, "Gallery")
            }
        }

        // --- NEW: Horizontal Media Preview ---
        if (selectedMediaPaths.isNotEmpty()) {
            LazyRow(modifier = Modifier.height(120.dp).fillMaxWidth()) {
                items(selectedMediaPaths) { path ->
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(path),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Delete button for media
                        IconButton(
                            onClick = { selectedMediaPaths.remove(path) },
                            modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(0.4f), CircleShape).size(24.dp)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {

                viewModel.saveNewLog(
                    title = title,
                    caption = description, // Matches your 'description' state
                    category = selectedCategory,
                    type = "memory",
                    mediaPaths = selectedMediaPaths.toList(),

                    colorHex = "#000000", // Or pass a dynamic color if you added a picker
                    scheduledAt = null,    // Or pass your date state
                    context = context      // This is the important one for file saving!
                )
                onSaveSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            enabled = title.isNotBlank(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Save Entry")
        }
    }
}