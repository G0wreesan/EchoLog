package com.echolog.app.util

import android.graphics.Bitmap

object ImageUtils {
    fun resizeForProfile(source: Bitmap): Bitmap {
        val size = 512
        return Bitmap.createScaledBitmap(source, size, size, true)
    }
}