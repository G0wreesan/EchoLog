package com.echolog.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey 
    val id: String = java.util.UUID.randomUUID().toString(),
    
    @SerialName("user_id") 
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val title: String,
    val caption: String?,
    val category: String,
    
    @SerialName("log_type") 
    @ColumnInfo(name = "log_type")
    val logType: String,

    // Local device sandbox tracking arrays - Marked @Transient to exclude from Supabase Postgrest serialization
    @Transient
    @SerialName("local_image_paths") 
    @ColumnInfo(name = "local_image_paths")
    val localImagePaths: List<String> = emptyList(),
    
    @Transient
    @SerialName("local_audio_paths") 
    @ColumnInfo(name = "local_audio_paths")
    val localAudioPaths: List<String> = emptyList(),
    
    @Transient
    @SerialName("local_video_paths") 
    @ColumnInfo(name = "local_video_paths")
    val localVideoPaths: List<String> = emptyList(),

    // Remote Supabase storage public URL bucket links
    @SerialName("remote_image_urls") 
    @ColumnInfo(name = "remote_image_urls")
    val remoteImageUrls: List<String> = emptyList(),
    
    @SerialName("remote_audio_urls") 
    @ColumnInfo(name = "remote_audio_urls")
    val remoteAudioUrls: List<String> = emptyList(),
    
    @SerialName("remote_video_urls") 
    @ColumnInfo(name = "remote_video_urls")
    val remoteVideoUrls: List<String> = emptyList(),

    @SerialName("scheduled_at") 
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: String? = null,
    
    @SerialName("created_at") 
    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @SerialName("is_synced") 
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
    
    @SerialName("color_hex") 
    @ColumnInfo(name = "color_hex")
    val colorHex: String = "#000000"
)

/**
 * Remote-specific representation of a Log to avoid sending local paths to Supabase.
 */
@Serializable
data class LogRemoteEntity(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val caption: String?,
    val category: String,
    @SerialName("log_type") val logType: String,
    @SerialName("remote_image_urls") val remoteImageUrls: List<String>,
    @SerialName("remote_audio_urls") val remoteAudioUrls: List<String>,
    @SerialName("remote_video_urls") val remoteVideoUrls: List<String>,
    @SerialName("scheduled_at") val scheduledAt: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_synced") val isSynced: Boolean,
    @SerialName("color_hex") val colorHex: String
)

fun LogEntity.toRemote() = LogRemoteEntity(
    id = id,
    userId = userId,
    title = title,
    caption = caption,
    category = category,
    logType = logType,
    remoteImageUrls = remoteImageUrls,
    remoteAudioUrls = remoteAudioUrls,
    remoteVideoUrls = remoteVideoUrls,
    scheduledAt = scheduledAt,
    createdAt = createdAt,
    isSynced = isSynced,
    colorHex = colorHex
)
