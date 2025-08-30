package com.example.data.mapper

import com.example.data.datasource.local.entity.NoteEntity
import com.example.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        imageUri = imageUri,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        imageUri = imageUri,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
