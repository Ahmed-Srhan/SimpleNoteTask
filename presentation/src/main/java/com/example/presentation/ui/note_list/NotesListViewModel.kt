package com.example.presentation.ui.note_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.use_case.DeleteNoteUseCase
import com.example.domain.use_case.GetNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val getNotes: GetNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<NoteListEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadNotes()
    }

    fun onEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.Load -> loadNotes()
            is NoteListEvent.Open -> navigateToEditor(event.id)
            is NoteListEvent.AddNew -> navigateToEditor(null)
            is NoteListEvent.LongClick -> showOptions(event.id)
            is NoteListEvent.ConfirmDelete -> delete(event.id)
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            getNotes()
                .onStart { _state.update { it.copy(isLoading = true, error = null) } }
                .distinctUntilChanged()
                .catch { ex ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = ex.localizedMessage ?: "Unknown error"
                        )
                    }
                }
                .collect { notes ->
                    _state.update { it.copy(isLoading = false, notes = notes, error = null) }
                }
        }
    }

    private fun navigateToEditor(id: Int?) = sendEffect(NoteListEffect.NavigateToEditor(id))
    private fun showOptions(id: Int) = sendEffect(NoteListEffect.ShowOptionsDialog(id))

    private fun delete(id: Int) {
        viewModelScope.launch {
            val note = state.value.notes.firstOrNull { it.id == id }
                ?: return@launch sendEffect(NoteListEffect.ShowToast("Note not found"))

            try {
                deleteNote(note)
                sendEffect(NoteListEffect.ShowToast("Note deleted successfully"))
                loadNotes()
            } catch (ex: Exception) {
                sendEffect(NoteListEffect.ShowToast("Failed to delete note: ${ex.localizedMessage}"))
            }
        }
    }

    private fun sendEffect(effect: NoteListEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }

}

