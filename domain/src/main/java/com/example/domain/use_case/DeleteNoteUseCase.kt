package com.example.domain.use_case

import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class DeleteNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(note: Note) = withContext(IO) {
        repo.deleteNote(note)
    }
}