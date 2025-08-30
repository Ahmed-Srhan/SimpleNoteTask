package com.example.presentation.ui.note_editor

import android.net.Uri


sealed interface NoteEditorEvent {
    data class Load(val id: Int?) : NoteEditorEvent
    data class TitleChanged(val value: String) : NoteEditorEvent
    data class ContentChanged(val value: String) : NoteEditorEvent
    data class AttachImageUri(val uri: Uri) : NoteEditorEvent   // result from picker/camera
    object ClearImage : NoteEditorEvent
    object Save : NoteEditorEvent
    object Delete : NoteEditorEvent
}

sealed interface NoteEditorEffect {
    object BackToList : NoteEditorEffect
    data class ShowToast(val text: String) : NoteEditorEffect
}
