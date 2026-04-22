package com.echolog.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Discovery : Screen("discovery", "Explore", Icons.Default.Search)
    object Create : Screen("create", "Post", Icons.Default.AddCircle)
    object Saved : Screen("saved", "Saved", Icons.Default.Favorite)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Discovery,
    Screen.Create,
    Screen.Saved,
    Screen.Profile
)