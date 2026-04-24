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
                val currentUserId = log.userId.ifBlank {
                    supabaseClient.auth.currentUserOrNull()?.id ?: "anonymous"
                }

                // 1. Save locally first to get internal paths
                // Using a loop instead of .map to avoid suspension inference issues
                val internalPaths = mutableListOf<String>()
                for (path in log.localMediaPaths) {
                    internalPaths.add(saveFileToInternalStorage(Uri.parse(path), context))
                }

                // 2. Upload to Supabase Storage
                val remoteUrls = mutableListOf<String>()
                for (localPath in internalPaths) {
                    val file = File(localPath)
                    val fileName = "$currentUserId/${file.name}"

                    // Explicitly calling the suspend function in the coroutine body
                    bucket.upload(path = fileName, data = file.readBytes()) {
                        upsert = true
                    }
                    remoteUrls.add(bucket.publicUrl(fileName))
                }

                // 3. Reconstruct LogEntity using the secondary constructor
                // match the exact order of parameters in your LogEntity file
                val logToSave = LogEntity(
                    id = log.id,
                    userId = log.userId,
                    title = log.title,
                    caption = log.caption,
                    category = log.category,
                    logType = log.logType,
                    remoteMediaUrls = remoteUrls,
                    scheduledAt = log.scheduledAt,
                    createdAt = log.createdAt,
                    isSynced = false,
                    colorHex = log.colorHex,
                    hasReminder = log.hasReminder
                )

                // 4. Persistence operations
                logDao.insertLog(logToSave)
                postgrest.from("logs").upsert(logToSave)
                logDao.markAsSynced(logToSave.id)

                android.util.Log.d("SYNC_SUCCESS", "Log and Media synced: ${logToSave.id}")

            } catch (e: Exception) {
                android.util.Log.e("SYNC_ERROR", "Step failed: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    suspend fun syncPendingLogs() {
        val pending = logDao.getUnsyncedLogs()
        if (pending.isEmpty()) return
        try {
            postgrest.from("logs").upsert(pending)
            pending.forEach { log -> logDao.markAsSynced(log.id) }
        } catch (e: Exception) {
            android.util.Log.e("SYNC_ERROR", "Sync failed: ${e.message}")
        }
    }

    suspend fun getUnsyncedLogs(): List<LogEntity> = logDao.getUnsyncedLogs()

    suspend fun updateSyncStatus(id: String, status: Boolean) = logDao.updateSyncStatus(id, status)

    fun getLogsByDate(userId: String, datePrefix: String): Flow<List<LogEntity>> =
        logDao.getLogsByDate(userId, datePrefix)

    private suspend fun saveFileToInternalStorage(uri: Uri, context: Context): String =
        withContext(Dispatchers.IO) {
            val extension = when (context.contentResolver.getType(uri)) {
                "video/mp4" -> "mp4"
                "audio/mpeg", "audio/mp3" -> "mp3"
                else -> "jpg"
            }
            val fileName = "media_${UUID.randomUUID()}.$extension"
            val file = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }
}