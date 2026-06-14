package com.echolog.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
    val context = LocalContext.current
    val isOnline = true // Replace with actual connectivity checking method utility if required

    var showEditProfile by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }

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
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditProfile = false
                }) { Text("Save", color = Color.Black) }
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
                val avatarUrl = userProfile?.avatar_url
                if (avatarUrl != null && avatarUrl.startsWith("http")) {
                    coil3.compose.AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(64.dp).background(Color(0xCC3FC1FD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userProfile?.display_name ?: "Echo User",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
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

    if (showEditProfile) {
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = newDisplayName,
                    onValueChange = { newDisplayName = it },
                    placeholder = { Text(userProfile?.display_name ?: "Enter name") },
                    singleLine = true
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
}
