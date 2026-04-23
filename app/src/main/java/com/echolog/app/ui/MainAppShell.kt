package com.echolog.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.echolog.app.viewmodel.LogViewModel
import com.echolog.app.viewmodel.RegistrationViewModel

@Composable
fun MainAppShell(
    registrationViewModel: RegistrationViewModel, // Renamed for clarity
    logViewModel: LogViewModel = hiltViewModel(), // Injected here
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                bottomNavItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            if (screen is Screen.Create) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Icon(screen.icon, contentDescription = null)
                            }
                        },
                        label = { if (screen !is Screen.Create) Text(screen.title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(logViewModel)
                1 -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("Calendar View Coming Soon")
                }
                2 -> CreateLogScreen(logViewModel) { selectedTab = 0 }
                3 -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text("Browse Logs")
                }
                4 -> ProfileAndSettingsScreen(registrationViewModel, onLogout)
            }
        }
    }
}



@Composable
fun ProfileAndSettingsScreen(viewModel: RegistrationViewModel, onLogout: () -> Unit) {
    val username by viewModel.username.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val selectedRes by viewModel.selectedAvatarRes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.size(110.dp).clip(CircleShape).background(Color.LightGray)) {
            if (selectedBitmap != null) {
                Image(bitmap = selectedBitmap!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else if (selectedRes != null) {
                Image(painter = painterResource(id = selectedRes!!), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("@$username", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(40.dp))

        SettingsRow(Icons.Default.Edit, "Edit Profile")
        SettingsRow(Icons.Default.Notifications, "Notifications")
        SettingsRow(Icons.Default.Lock, "Privacy & Security")
        SettingsRow(Icons.Default.Info, "Help Center")

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout from EchoLog", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.DarkGray, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
    }
}