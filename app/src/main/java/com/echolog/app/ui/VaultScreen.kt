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

        Button(
            onClick = { logViewModel.syncLocalLogsToSupabase(context) },
            enabled = !isSyncing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSyncing) "Syncing Data..." else "Sync Now")
        }
    }
}