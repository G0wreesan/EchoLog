package com.echolog.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Create : Screen("create", "Add", Icons.Default.AddCircle)
    object Browse : Screen("browse", "Browse", Icons.Default.List)
    object Profile : Screen("profile", "Vault", Icons.Default.Lock)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Create,
    Screen.Browse,
    Screen.Profile
)