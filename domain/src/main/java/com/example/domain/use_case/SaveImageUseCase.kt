package com.example.domain.use_case

import android.net.Uri
import com.example.domain.repo.NoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class SaveImageUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(uri: Uri): String = withContext(IO) {
        repo.saveImage(uri)
    }
}