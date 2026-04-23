package com.echolog.app.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val display_name: String,
    val email: String,
    val date_of_birth: String,
    val avatar_url: String? = null
)

@Serializable
data class LogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val user_id: String,
    val title: String,
    val caption: String? = null,
    val image_urls: List<String> = emptyList(),
    val video_urls: List<String> = emptyList(),
    val voice_url: String? = null,
    val log_type: String = "memory",
    val category: String = "General",
    val notif_color: String = "#000000",
    val repeat_option: String = "none",
    val scheduled_at: String? = null,
    val created_at: String? = null,
    val isSynced: Boolean = false // UI uses this for Cloud/Sync icon
)