package com.example.domain.repo

import android.net.Uri
import com.example.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Int): Note?
    suspend fun addOrUpdateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun saveImage(uri: Uri): String
}
