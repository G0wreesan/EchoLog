package com.echolog.app.data

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val logDao: LogDao,
    private val postgrest: Postgrest
) {
    fun getLogsForUser(userId: String): Flow<List<LogEntity>> = logDao.getLogsForUser(userId)

    suspend fun saveAndSyncLog(log: LogEntity, context: Context) {
        val internalPaths = log.localMediaPaths.map { uriString ->
            saveFileToInternalStorage(Uri.parse(uriString), context)
        }
        val logWithPaths = log.copy(localMediaPaths = internalPaths)

        // 1. Save locally first (This is working)
        logDao.insertLog(logWithPaths)

        // 2. Try to Sync
        try {
            // Use .from().insert() which is more explicit
            postgrest.from("logs").insert(logWithPaths)

            // 3. Only mark as synced if the line above didn't throw an exception
            logDao.markAsSynced(logWithPaths.id)
            android.util.Log.d("SYNC_SUCCESS", "Log synced to cloud: ${logWithPaths.id}")
        } catch (e: Exception) {
            // IMPORTANT: This will tell you EXACTLY why it's not uploading
            android.util.Log.e("SYNC_ERROR", "Failed to upload log: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun getUnsyncedLogs(): List<LogEntity> {
        return logDao.getUnsyncedLogs()
    }
    suspend fun updateSyncStatus(id: String, status: Boolean) {
        logDao.updateSyncStatus(id, status)
    }

    suspend fun syncPendingLogs() {
        val pending = logDao.getUnsyncedLogs()
        if (pending.isEmpty()) return

        try {
            // Use from("logs") and upsert.
            // Supabase-kt handles the mapping of Kotlin List to PostgreSQL TEXT[] automatically via @Serializable
            postgrest.from("logs").upsert(pending)

            pending.forEach { log ->
                logDao.markAsSynced(log.id)
            }
        } catch (e: Exception) {
            android.util.Log.e("SYNC_ERROR", "Supabase sync failed: ${e.message}")
        }
    }

    private suspend fun saveFileToInternalStorage(uri: Uri, context: Context): String =
        withContext(Dispatchers.IO) {
            val fileName = "media_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file.absolutePath
        }
}