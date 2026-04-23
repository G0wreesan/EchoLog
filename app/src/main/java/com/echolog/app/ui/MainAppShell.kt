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
        containerColor = Color(0xCC3FC1FD), // ✅ App background color
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xCC3FC1FD),
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            if (screen is Screen.Create) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(50.dp)
                                )
                            } else {
                                Icon(screen.icon, contentDescription = null, tint = Color.White)
                            }
                        },
                        label = { if (screen !is Screen.Create) Text(screen.title) }
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC3FC1FD)) // ✅ ensures full background coverage
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(logViewModel)
                1 -> Column(
                    Modifier.fillMaxSize(),
                    Arrangement.Center,
                    Alignment.CenterHorizontally
                ) {
                    Text("Calendar View Coming Soon")
                }
                2 -> CreateLogScreen(logViewModel) { selectedTab = 0 }
                3 -> BrowseScreen(logViewModel)
                4 -> VaultScreen(
                    viewModel = registrationViewModel,
                    logViewModel = logViewModel,
                    onLogout = onLogout
                )
            }
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
        Text(
            title,
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White)
    }
}