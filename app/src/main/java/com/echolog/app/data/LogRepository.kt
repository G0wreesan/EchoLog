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
        // Handle media saving before inserting
        val internalPaths = log.localMediaPaths.map { uriString ->
            saveFileToInternalStorage(Uri.parse(uriString), context)
        }
        val logWithPaths = log.copy(localMediaPaths = internalPaths)

        logDao.insertLog(logWithPaths)

        try {
            postgrest["logs"].insert(logWithPaths)
            logDao.markAsSynced(logWithPaths.id)
        } catch (e: Exception) {
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
        val pending: List<LogEntity> = logDao.getUnsyncedLogs()
        pending.forEach { log: LogEntity ->
            try {
                postgrest["logs"].insert(log)
                logDao.markAsSynced(log.id)
            } catch (e: Exception) { }
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