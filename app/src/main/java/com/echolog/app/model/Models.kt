package com.echolog.app.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val display_name: String,
    val email: String,
    val date_of_birth: String, // Matches "date_of_birth" in your SQL
    val avatar_url: String? = null
)