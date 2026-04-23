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
    val allLogs: Flow<List<LogEntity>> = logDao.getAllLogs()

    suspend fun saveAndSyncLog(log: LogEntity, context: Context) {
        val internalPaths = log.localMediaPaths.map { uriString ->
            saveFileToInternalStorage(Uri.parse(uriString), context)
        }

        val finalLog = log.copy(localMediaPaths = internalPaths)
        logDao.insertLog(finalLog)

        try {
            postgrest["logs"].insert(finalLog) // Use finalLog with correct paths
            logDao.markAsSynced(finalLog.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun saveFileToInternalStorage(uri: Uri, context: Context): String = withContext(Dispatchers.IO) {
        val fileName = "media_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return@withContext file.absolutePath
    }
}