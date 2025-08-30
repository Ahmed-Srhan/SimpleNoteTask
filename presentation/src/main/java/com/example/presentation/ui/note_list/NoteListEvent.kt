package com.example.presentation.ui.note_list

sealed interface NoteListEvent {
    object Load : NoteListEvent
    data class Open(val id: Int) : NoteListEvent
    object AddNew : NoteListEvent
    data class LongClick(val id: Int) : NoteListEvent
    data class ConfirmDelete(val id: Int) : NoteListEvent
}

sealed interface NoteListEffect {
    data class NavigateToEditor(val id: Int?) : NoteListEffect
    data class ShowToast(val text: String) : NoteListEffect
    data class ShowOptionsDialog(val id: Int) : NoteListEffect

}