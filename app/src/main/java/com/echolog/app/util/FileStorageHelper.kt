package com.echolog.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileStorageHelper {

    /**
     * Safely saves a file to internal app storage, handling both content URIs
     * from the gallery and raw file path strings from the camera.
     */
    fun saveFileToInternalStorage(
        context: Context,
        sourcePathOrUri: String,
        prefix: String,
        extension: String
    ): String {
        return try {
            val targetDir = File(context.filesDir, "media").apply { mkdirs() }
            val permanentFile = File(targetDir, "${prefix}_${System.currentTimeMillis()}.$extension")

            if (sourcePathOrUri.startsWith("content://") || sourcePathOrUri.startsWith("file://")) {
                // Handle Gallery / Content Resolver URIs
                val uri = Uri.parse(sourcePathOrUri)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(permanentFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // Handle raw camera file paths (/data/user/0/...)
                val sourceFile = File(sourcePathOrUri)
                if (sourceFile.exists()) {
                    sourceFile.inputStream().use { input ->
                        FileOutputStream(permanentFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    // Fallback to current path if source file does not exist
                    return sourcePathOrUri
                }
            }
            permanentFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            sourcePathOrUri
        }
    }
}