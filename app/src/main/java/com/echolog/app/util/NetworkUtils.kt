package com.echolog.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.compose.runtime.*

@Composable
fun isNetworkAvailable(context: Context): State<Boolean> {
    // 1. Get the connectivity manager from the system services
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // 2. Create a state that Compose can observe
    val isConnected = remember { mutableStateOf(false) }

    // 3. Define the callback that reacts to network changes
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isConnected.value = true
        }

        override fun onLost(network: Network) {
            isConnected.value = false
        }
    }

    // 4. Use DisposableEffect to register/unregister the listener safely
    DisposableEffect(Unit) {
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected
}