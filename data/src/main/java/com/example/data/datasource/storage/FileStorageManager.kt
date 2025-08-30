package com.example.data.datasource.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun saveImage(uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "${System.currentTimeMillis()}.jpg")
        input?.use { i -> file.outputStream().use { i.copyTo(it) } }
        return file.absolutePath
    }

    fun deleteImage(path: String) {
        File(path).delete()
    }
}
