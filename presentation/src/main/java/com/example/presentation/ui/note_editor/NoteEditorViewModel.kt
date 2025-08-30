package com.example.presentation.ui.note_editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Note
import com.example.domain.use_case.AddOrUpdateNoteUseCase
import com.example.domain.use_case.DeleteNoteUseCase
import com.example.domain.use_case.GetNoteByIdUseCase
import com.example.domain.use_case.SaveImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val getNoteById: GetNoteByIdUseCase,
    private val addOrUpdateNote: AddOrUpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val saveImage: SaveImageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<NoteEditorEffect>()
    val effect: SharedFlow<NoteEditorEffect> = _effect.asSharedFlow()

    private val noteIdArg: Int? = savedStateHandle.get<Int>("noteId")?.takeIf { it != -1 }

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            val initial = noteIdArg?.let { getNoteById(it) } ?: emptyNote()
            _state.update { it.copy(note = initial) }
        }
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.TitleChanged -> updateNote { it.copy(title = event.value) }
            is NoteEditorEvent.ContentChanged -> updateNote { it.copy(content = event.value) }
            is NoteEditorEvent.AttachImageUri -> attachImage(event.uri)
            is NoteEditorEvent.ClearImage -> updateNote { it.copy(imageUri = null) }
            is NoteEditorEvent.Save -> saveNote()
            is NoteEditorEvent.Delete -> deleteNote()
            is NoteEditorEvent.Load -> {}
        }
    }

    private fun updateNote(transform: (Note) -> Note) {
        _state.update { it.copy(note = transform(it.note)) }
    }

    private fun setLoading(isLoading: Boolean) {
        _state.update { it.copy(isSaving = isLoading) }
    }

    private fun attachImage(uri: Uri) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val storedPath = saveImage(uri)
                updateNote { it.copy(imageUri = storedPath) }
            } catch (ex: Exception) {
                _effect.emit(NoteEditorEffect.ShowToast("Failed to attach image: ${ex.localizedMessage}"))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun saveNote() {
        viewModelScope.launch {
            val note = _state.value.note
            try {
                setLoading(true)
                val now = System.currentTimeMillis()
                val noteToSave = note.copy(
                    createdAt = if (note.id == 0) now else note.createdAt,
                    updatedAt = now
                )
                addOrUpdateNote(noteToSave)
                _effect.emit(NoteEditorEffect.BackToList)
            } catch (ex: IllegalArgumentException) {
                _effect.emit(NoteEditorEffect.ShowToast(ex.message ?: "Invalid note"))
            } catch (ex: Exception) {
                _effect.emit(NoteEditorEffect.ShowToast("Save failed: ${ex.localizedMessage}"))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun deleteNote() {
        viewModelScope.launch {
            val note = _state.value.note
            if (note.id == 0) {
                _effect.emit(NoteEditorEffect.ShowToast("No note to delete"))
                return@launch
            }

            try {
                setLoading(true)
                deleteNote(note)
                _effect.emit(NoteEditorEffect.BackToList)
            } catch (ex: Exception) {
                _effect.emit(NoteEditorEffect.ShowToast("Delete failed: ${ex.localizedMessage}"))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun emptyNote() = Note(
        id = 0,
        title = null,
        content = "",
        imageUri = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

