package com.example.presentation.ui.note_list

import com.example.domain.model.Note

data class NoteListState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val error: String? = null
)