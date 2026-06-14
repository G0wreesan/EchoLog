package com.echolog.app.data

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val logDao: LogDao,
    private val supabaseClient: SupabaseClient
) {
    private val postgrest = supabaseClient.postgrest
    private val bucket = supabaseClient.storage.from("vault")

    fun getLogsForUser(userId: String): Flow<List<LogEntity>> = logDao.getLogsForUser(userId)

    suspend fun saveAndSyncLog(log: LogEntity, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = if (log.userId.isBlank() || log.userId == "anonymous") {
                    supabaseClient.auth.currentUserOrNull()?.id ?: "anonymous"
                } else {
                    log.userId
                }

                // 1. Upload Files to Supabase Storage Buckets
                // We use the local paths already stored in the log object
                val newRemoteImages = uploadFilesToBucket(log.localImagePaths, currentUserId)
                val newRemoteAudios = uploadFilesToBucket(log.localAudioPaths, currentUserId)
                val newRemoteVideos = uploadFilesToBucket(log.localVideoPaths, currentUserId)

                // 2. Build completely type-safe LogEntity model representation
                val logToSave = log.copy(
                    userId = currentUserId,
                    remoteImageUrls = (log.remoteImageUrls + newRemoteImages).distinct(),
                    remoteAudioUrls = (log.remoteAudioUrls + newRemoteAudios).distinct(),
                    remoteVideoUrls = (log.remoteVideoUrls + newRemoteVideos).distinct(),
                    isSynced = false
                )

                // 3. Save locally to Room database
                logDao.insertLog(logToSave)

                // 4. Upsert to Supabase Postgrest if not anonymous
                if (currentUserId != "anonymous") {
                    try {
                        postgrest.from("logs").upsert(logToSave.toRemote())
                        logDao.markAsSynced(logToSave.id)
                        android.util.Log.d("SYNC_SUCCESS", "Synced to Supabase successfully.")
                    } catch (e: Exception) {
                        android.util.Log.e("SYNC_ERROR", "Supabase upsert failed: ${e.localizedMessage}")
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("SYNC_ERROR", "Global save failed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun uploadFilesToBucket(filePaths: List<String>, userId: String): List<String> {
        val uploadedUrls = mutableListOf<String>()
        for (localPath in filePaths) {
            if (localPath.startsWith("http")) {
                uploadedUrls.add(localPath)
                continue
            }
            val file = File(localPath)
            if (file.exists()) {
                try {
                    val fileName = "$userId/${file.name}"
                    bucket.upload(path = fileName, data = file.readBytes()) {
                        upsert = true
                    }
                    uploadedUrls.add(bucket.publicUrl(fileName))
                } catch (e: Exception) {
                    android.util.Log.e("UPLOAD_ERROR", "Failed to upload ${file.name}: ${e.message}")
                }
            }
        }
        return uploadedUrls
    }

    suspend fun syncPendingLogs() {
        val pending = logDao.getUnsyncedLogs()
        if (pending.isEmpty()) return
        try {
            postgrest.from("logs").upsert(pending.map { it.toRemote() })
            pending.forEach { log -> logDao.markAsSynced(log.id) }
        } catch (e: Exception) {
            android.util.Log.e("SYNC_ERROR", "Sync failed: ${e.message}")
        }
    }

    suspend fun getUnsyncedLogs(): List<LogEntity> = logDao.getUnsyncedLogs()

    suspend fun updateSyncStatus(id: String, status: Boolean) = logDao.updateSyncStatus(id, status)

    fun getLogsByDate(userId: String, datePrefix: String): Flow<List<LogEntity>> =
        logDao.getLogsByDate(userId, datePrefix)

    private suspend fun saveFileToInternalStorage(uri: Uri, context: Context, fallbackExtension: String): String =
        withContext(Dispatchers.IO) {
            val extension = when (context.contentResolver.getType(uri)) {
                "video/mp4" -> "mp4"
                "audio/mpeg", "audio/mp3" -> "mp3"
                else -> fallbackExtension
            }
            val fileName = "media_${UUID.randomUUID()}.$extension"
            val file = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }
}