package com.example.presentation.ui.note_editor

import com.example.domain.model.Note


data class NoteEditorState(
    val note: Note = Note(
        0,
        null,
        "",
        null,
        System.currentTimeMillis(),
        System.currentTimeMillis()
    ),
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)