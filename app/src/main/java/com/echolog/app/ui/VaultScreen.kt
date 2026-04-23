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
import coil3.compose.AsyncImage
import com.echolog.app.R
import com.echolog.app.util.isNetworkAvailable
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
    val isOnline by isNetworkAvailable(context)

    // State for Dialogs
    var showEditProfile by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }

    // Dynamic Greeting
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    // Edit Profile Dialog
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
                    // viewModel.updateDisplayName(newDisplayName) // Implement in RegistrationViewModel
                    showEditProfile = false
                }) { Text("Save", color = Color.Black) }
            }
        )
    }

    // Support Bottom Sheet / Dialog
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

        // Header Section
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
                    Text(if (isOnline) "Synced" else "Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isOnline) Color(0xFF2E7D32) else Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Profile Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(100.dp)) {
                val avatarUrl = userProfile?.avatar_url
                if (avatarUrl?.startsWith("local_res_") == true) {
                    val resId = avatarUrl.removePrefix("local_res_").toIntOrNull() ?: R.drawable.avatar_1
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color.Black, CircleShape)
                    )
                } else {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.avatar_1),
                        modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, Color.Black, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(userProfile?.display_name ?: "Echo User", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("@${userProfile?.username ?: "username"}", fontSize = 15.sp, color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text("Account Settings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

        // Action Rows with Functionality
        VaultActionRow(Icons.Default.Settings, "App Preferences") {
            newDisplayName = userProfile?.display_name ?: ""
            showEditProfile = true
        }

        VaultActionRow(
            icon = Icons.Default.CloudSync,
            label = if (isSyncing) "Syncing..." else "Sync Data Now"
        ) {
            if (isOnline) logViewModel.syncLocalLogsToSupabase()
        }

        VaultActionRow(Icons.Default.VerifiedUser, "Security & Privacy") {
            // Navigate to security settings
        }

        VaultActionRow(Icons.Default.HelpOutline, "Support") {
            showSupport = true
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.Red, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Logout Session", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VaultActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Black, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}