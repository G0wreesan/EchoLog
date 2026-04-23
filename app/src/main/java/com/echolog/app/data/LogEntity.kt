package com.echolog.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val caption: String?,
    val category: String,
    val logType: String,
    val localMediaPaths: List<String>,
    val remoteMediaUrls: List<String> = emptyList(),
    val scheduledAt: Long?, // This is your reminder/notification date
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val colorHex: String = "#000000",
    val hasReminder: Boolean = false
)

// Add this to handle the List<String> in Room
class Converters {
    @TypeConverter
    fun fromList(value: List<String>) = Json.encodeToString(value)
    @TypeConverter
    fun toList(value: String) = Json.decodeFromString<List<String>>(value)
}