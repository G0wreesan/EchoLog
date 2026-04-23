package com.echolog.app.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.util.UUID
import kotlinx.datetime.Instant

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

    @SerialName("image_urls")
    val remoteMediaUrls: List<String> = emptyList(),

    // CHANGED TO STRING
    @SerialName("scheduled_at")
    val scheduledAt: String?,

    // CHANGED TO STRING
    @SerialName("created_at")
    val createdAt: String = java.time.Instant.now().toString(),

    @SerialName("is_synced")
    val isSynced: Boolean = false,

    @SerialName("notif_color")
    val colorHex: String = "#000000",

    @SerialName("has_reminder")
    val hasReminder: Boolean = false,

    @Ignore @Transient
    val localMediaPaths: List<String> = emptyList()
) {
    // UPDATED secondary constructor for Room to match String types
    constructor(
        id: String,
        userId: String,
        title: String,
        caption: String?,
        category: String,
        logType: String,
        remoteMediaUrls: List<String>,
        scheduledAt: String?,
        createdAt: String,
        isSynced: Boolean,
        colorHex: String,
        hasReminder: Boolean
    ) : this(
        id = id,
        userId = userId,
        title = title,
        caption = caption,
        category = category,
        logType = logType,
        remoteMediaUrls = remoteMediaUrls,
        scheduledAt = scheduledAt,
        createdAt = createdAt,
        isSynced = isSynced,
        colorHex = colorHex,
        hasReminder = hasReminder,
        localMediaPaths = emptyList()
    )
}

class Converters {
    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = try {
        Json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }
}