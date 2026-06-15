package com.echolog.app.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("echolog_prefs", Context.MODE_PRIVATE)

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()

    var digestTime: String
        get() = prefs.getString("digest_time", "08:00") ?: "08:00"
        set(value) = prefs.edit().putString("digest_time", value).apply()
}
