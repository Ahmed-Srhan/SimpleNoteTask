package com.example.domain.use_case

import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class AddOrUpdateNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(note: Note) = withContext(IO) {
        val titleEmpty = note.title.isNullOrBlank()
        val contentEmpty = note.content.isBlank()
        if (titleEmpty && contentEmpty) {
            throw IllegalArgumentException("Note must have at least a title or content")
        }
        repo.addOrUpdateNote(note)
    }
}