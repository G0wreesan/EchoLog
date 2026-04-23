package com.echolog.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString


@Entity(tableName = "logs")
@Serializable
data class LogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @SerialName("user_id")
    val userId: String,

    val title: String,
    val caption: String?,
    val category: String,

    @SerialName("log_type")
    val logType: String,

    @Transient // Excluded from Supabase
    val localMediaPaths: List<String> = emptyList(),

    @SerialName("image_urls")
    val remoteMediaUrls: List<String> = emptyList(),

    @SerialName("scheduled_at")
    val scheduledAt: Long?,

    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @SerialName("is_synced")
    val isSynced: Boolean = false,

    @SerialName("notif_color")
    val colorHex: String = "#000000",

    @SerialName("has_reminder") // Move this inside the class
    val hasReminder: Boolean = false
)

// Add this to handle the List<String> in Room
class Converters {
    @TypeConverter
    fun fromList(value: List<String>) = Json.encodeToString(value)
    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<List<String>>(value)
}