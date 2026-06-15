package com.echolog.app.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echolog.app.viewmodel.LogViewModel
import com.echolog.app.viewmodel.RegistrationViewModel
import com.echolog.app.R
import android.app.TimePickerDialog
import com.echolog.app.util.PreferenceManager
import com.echolog.app.util.ReminderScheduler
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: RegistrationViewModel,
    logViewModel: LogViewModel,
    onLogout: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isSyncing by logViewModel.isSyncing.collectAsState()
    val isUpdatingAvatar by viewModel.isChecking.collectAsState()
    val context = LocalContext.current
    val isOnline = true // Replace with actual connectivity checking method utility if required

    val prefs = remember { PreferenceManager(context) }
    var notificationsEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }
    var digestTime by remember { mutableStateOf(prefs.digestTime) }

    var showEditProfile by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, it)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            val scaled = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
            viewModel.setCustomBitmap(scaled)
            viewModel.updateAvatar { showAvatarPicker = false }
        }
    }

    val defaultAvatars = listOf(
        R.drawable.avatar_1, R.drawable.canva_avatar_2, R.drawable.canva_avatar_3,
        R.drawable.canva_avatar_4, R.drawable.canva_avatar_5, R.drawable.canva_avatar_6,
        R.drawable.canva_avatar_7, R.drawable.canva_avatar_8, R.drawable.canva_avatar_9,
        R.drawable.canva_avatar_10, R.drawable.canva_avatar_11, R.drawable.canva_avatar_12,
        R.drawable.canva_avatar_13, R.drawable.canva_avatar_14
    )

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    if (showEditProfile) {
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = newDisplayName,
                    onValueChange = { newDisplayName = it },
                    placeholder = { Text(userProfile?.display_name ?: "Enter name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xCC3FC1FD))
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newDisplayName.isNotBlank()) {
                        viewModel.updateDisplayName(newDisplayName)
                    }
                    showEditProfile = false
                }) { Text("Save", color = Color(0xCC3FC1FD)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }

    if (showSupport) {
        AlertDialog(
            onDismissRequest = { showSupport = false },
            title = { Text("App Support") },
            text = {
                Column {
                    Text("• Memories are stored locally first.")
                    Text("• Use 'Sync Now' to backup to the cloud.")
                    Text("• Use the Vault to manage security.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupport = false }) { Text("Close") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(greeting, fontSize = 14.sp, color = Color.Gray)
                Text("Your Vault", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }

            Surface(
                color = if (isOnline) Color(0xFFF1F8E9) else Color(0xFFFFF1F1),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(if (isOnline) Color(0xFF4CAF50) else Color.Red, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isOnline) "Online" else "Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isOnline) Color(0xFF2E7D32) else Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture (moved aside from the indicator)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable { showAvatarPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = userProfile?.avatar_url
                    when {
                        avatarUrl?.startsWith("http") == true -> {
                            coil3.compose.AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        avatarUrl?.startsWith("local_res_") == true -> {
                            val resId = avatarUrl.removePrefix("local_res_").toIntOrNull()
                            if (resId != null) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color(0xCC3FC1FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }

                    if (isUpdatingAvatar) {
                        CircularProgressIndicator(modifier = Modifier.fillMaxSize(), color = Color.White, strokeWidth = 2.dp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userProfile?.display_name ?: "Echo User",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Change profile picture button aside from the picture
                        IconButton(
                            onClick = { showAvatarPicker = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = userProfile?.email ?: "No email",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    newDisplayName = userProfile?.display_name ?: ""
                    showEditProfile = true
                }) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xCC3FC1FD))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Section
        Text("Settings", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // Notifications Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff, null)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reminders", fontWeight = FontWeight.Medium)
                    Text(
                        if (notificationsEnabled) "Active for today's logs" else "All notifications disabled",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        prefs.notificationsEnabled = it
                        if (!it) {
                            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            notificationManager.cancelAll()
                        } else {
                            ReminderScheduler.scheduleDailyDigest(context)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xCC3FC1FD))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Digest Time Picker
        Surface(
            modifier = Modifier.fillMaxWidth().clickable {
                val parts = digestTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                TimePickerDialog(context, { _, h, m ->
                    val newTime = String.format("%02d:%02d", h, m)
                    digestTime = newTime
                    prefs.digestTime = newTime
                    if (notificationsEnabled) {
                        ReminderScheduler.scheduleDailyDigest(context)
                    }
                }, hour, minute, true).show()
            },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, null)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Daily Digest Time", fontWeight = FontWeight.Medium)
                    Text("Time to pop up today's logs", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(digestTime, fontWeight = FontWeight.Bold, color = Color(0xCC3FC1FD))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Actions", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // Sync Now Button
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(enabled = !isSyncing) {
                logViewModel.syncLocalLogsToSupabase(context)
            },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Sync, null, tint = if (isSyncing) Color.Gray else Color.Black)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isSyncing) "Syncing..." else "Sync Now",
                    fontWeight = FontWeight.Medium,
                    color = if (isSyncing) Color.Gray else Color.Black
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xCC3FC1FD))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Support Button
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { showSupport = true },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.HelpOutline, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(16.dp))
                Text("App Support", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout Button
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onLogout() },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFEBEE)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Logout", fontWeight = FontWeight.Medium, color = Color.Red)
            }
        }
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Change Avatar") },
            text = {
                Column {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(defaultAvatars.size) { index ->
                            val resId = defaultAvatars[index]
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.selectAvatar(resId)
                                        viewModel.updateAvatar { showAvatarPicker = false }
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Custom Photo")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarPicker = false }) { Text("Cancel") }
            }
        )
    }
}
