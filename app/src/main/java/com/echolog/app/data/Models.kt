package com.echolog.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String, //  Supabase User UID
    val username: String,
    val display_name: String,
    val email: String,
    val date_of_birth: String,
    val avatar_url: String? = null
)

@Serializable
data class InterestCategory(
    val id: String,
    val name: String,
    val icon: String
)

val studentInterests = listOf(
    InterestCategory("music", "Music", "🎵"),
    InterestCategory("sports", "Sports", "⚽"),
    InterestCategory("workout", "Workout", "💪"),
    InterestCategory("yoga", "Yoga", "🧘"),
    InterestCategory("running", "Running", "🏃"),
    InterestCategory("nature", "Nature", "🌿"),
    InterestCategory("pets", "Pets & Animals", "🐾"),
    InterestCategory("news", "News", "📰"),
    InterestCategory("society", "Society", "⚖️"),
    InterestCategory("ads", "Advertisements", "📢"),
    InterestCategory("food", "Food", "🍕"),
    InterestCategory("cars", "Cars", "🚗"),
    InterestCategory("comedy", "Comedy", "😂"),
    InterestCategory("pics", "Pictures", "📸"),
    InterestCategory("voice", "Voice Notes", "🎤")
)