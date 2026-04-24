package com.echolog.app.data

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import io.github.jan.supabase.auth.auth

class LogRepository @Inject constructor(
    private val logDao: LogDao,
    private val postgrest: Postgrest,
    private val supabase: SupabaseClient
) {
    // Reference to your Supabase bucket
    private val bucket = supabase.storage.from("vault")

    fun getLogsForUser(userId: String): Flow<List<LogEntity>> = logDao.getLogsForUser(userId)

    /**
     * Main function to save log locally and sync to Supabase (Storage + Database)
     */
    suspend fun saveAndSyncLog(log: LogEntity, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                // Check if user is logged in before starting
                val userId = log.userId.ifBlank {
                    supabase.auth.currentUserOrNull()?.id ?: "anonymous"
                }

                // 1. Save files to internal storage (Local persistence)
                val internalPaths = log.localMediaPaths.map { uriString ->
                    saveFileToInternalStorage(Uri.parse(uriString), context)
                }

                // 2. Upload files to Supabase Storage and get remote URLs
                val remoteUrls = internalPaths.map { localPath ->
                    val file = File(localPath)
                    val fileName = "${log.userId}/${file.name}" // Organize bucket by userId

                    // Upload binary data
                    bucket.upload(path = fileName, data = file.readBytes()) {
                        upsert = true
                    }

                    // Return the public URL for the database
                    bucket.publicUrl(fileName)
                }

                // 3. Create a final version of the log with all paths/URLs
                val logToSave = log.copy(
                    localMediaPaths = internalPaths,
                    remoteMediaUrls = remoteUrls,
                    isSynced = false
                )

                // 4. Save to Local Room DB
                logDao.insertLog(logToSave)

                // 5. Sync metadata to Supabase Postgrest
                postgrest.from("logs").insert(logToSave)

                // 6. Mark as synced locally
                logDao.markAsSynced(logToSave.id)

                android.util.Log.d("SYNC_SUCCESS", "Media uploaded and Log synced: ${logToSave.id}")

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
            // Note: This logic assumes media is already uploaded or is handled
            // in a background worker. For now, it upserts the metadata.
            postgrest.from("logs").upsert(pending)
            pending.forEach { log ->
                logDao.markAsSynced(log.id)
            }
        } catch (e: Exception) {
            android.util.Log.e("SYNC_ERROR", "Supabase sync failed: ${e.message}")
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