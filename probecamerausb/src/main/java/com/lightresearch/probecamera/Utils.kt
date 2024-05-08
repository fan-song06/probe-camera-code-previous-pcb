package com.lightresearch.probecamera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.InputStream

class Utils {
    companion object {
        fun convertToTimestamp(seconds: Float): String {
            val wholeSeconds = seconds.toInt()
            val minutes = (wholeSeconds % 3600) / 60
            val secs = wholeSeconds % 60
            val milliseconds = ((seconds - wholeSeconds) * 1000).toInt()

            return String.format("%02d:%02d", minutes, secs, milliseconds)
        }
        fun saveImageToSharedMediaStorage(context: Context, bitmap: Bitmap, filename: String, mimeType: String = "image/jpeg"): Uri? {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
            }

            return uri
        }

        fun saveVideoToSharedMediaStorage(
            context: Context,
            videoInputStream: InputStream,
            filename: String,
            mimeType: String = "video/mp4"
        ): Uri? {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            }

            val uri: Uri? = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    videoInputStream.copyTo(outputStream)
                }
            }

            return uri
        }
    }

}
