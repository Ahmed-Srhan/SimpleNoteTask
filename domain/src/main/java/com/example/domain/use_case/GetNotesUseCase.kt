package com.example.domain.use_case

import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GetNotesUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(): Flow<List<Note>> = withContext(IO) {
        repo.getAllNotes()
    }
}