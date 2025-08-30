package com.example.data.repo

import android.net.Uri
import com.example.data.datasource.local.dao.NoteDao
import com.example.data.datasource.storage.FileStorageManager
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val fileStorageManager: FileStorageManager
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(id: Int): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun addOrUpdateNote(note: Note) =
        noteDao.insert(note.toEntity())

    override suspend fun deleteNote(note: Note) {
        noteDao.delete(note.toEntity())
        note.imageUri?.let { fileStorageManager.deleteImage(it) }
    }

    override suspend fun saveImage(uri: Uri): String =
        fileStorageManager.saveImage(uri)
}
