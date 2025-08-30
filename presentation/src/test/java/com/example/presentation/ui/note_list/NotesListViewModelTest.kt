import app.cash.turbine.test
import com.example.domain.model.Note
import com.example.domain.use_case.DeleteNoteUseCase
import com.example.domain.use_case.GetNotesUseCase
import com.example.presentation.ui.note_list.NoteListEffect
import com.example.presentation.ui.note_list.NoteListEvent
import com.example.presentation.ui.note_list.NotesListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModelTest {

    private lateinit var getNotes: GetNotesUseCase
    private lateinit var deleteNote: DeleteNoteUseCase
    private lateinit var viewModel: NotesListViewModel

    @Before
    fun setup() {
        getNotes = mockk()
        deleteNote = mockk(relaxed = true)
    }

    @Test
    fun `when init then load notes successfully`() = runTest {
        // Arrange
        val notes = listOf(
            Note(
                id = 1,
                title = "Test",
                content = "Content",
                imageUri = null,
                createdAt = 0L,
                updatedAt = 0L
            )
        )
        coEvery { getNotes() } returns flowOf(notes)

        // Act
        val viewModel = NotesListViewModel(getNotes, deleteNote)

        // Assert
        viewModel.state.test {
            awaitItem()
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(notes, state.notes)
        }
    }

    @Test
    fun `when getNotes throws error directly then state has error`() = runTest {
        coEvery { getNotes() } returns flow {
            throw RuntimeException("DB error")
        }

        viewModel = NotesListViewModel(getNotes, deleteNote)

        advanceUntilIdle()
        viewModel.state.test {
            val errorState = awaitItem() // Error state
            assertEquals("DB error", errorState.error)
            assertFalse(errorState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when Open event then emits NavigateToEditor effect`() = runTest {
        coEvery { getNotes() } returns flow { emit(emptyList()) }
        viewModel = NotesListViewModel(getNotes, deleteNote)

        viewModel.effect.test {
            viewModel.onEvent(NoteListEvent.Open(5))
            assertEquals(NoteListEffect.NavigateToEditor(5), awaitItem())
        }
    }

    @Test
    fun `when AddNew event then emits NavigateToEditor null`() = runTest {
        coEvery { getNotes() } returns flow { emit(emptyList()) }
        viewModel = NotesListViewModel(getNotes, deleteNote)

        viewModel.effect.test {
            viewModel.onEvent(NoteListEvent.AddNew)
            assertEquals(NoteListEffect.NavigateToEditor(null), awaitItem())
        }
    }

    @Test
    fun `when LongClick event then emits ShowOptionsDialog`() = runTest {
        coEvery { getNotes() } returns flow { emit(emptyList()) }
        viewModel = NotesListViewModel(getNotes, deleteNote)

        viewModel.effect.test {
            viewModel.onEvent(NoteListEvent.LongClick(7))
            assertEquals(NoteListEffect.ShowOptionsDialog(7), awaitItem())
        }
    }

    @Test
    fun `when ConfirmDelete for existing note then deletes and reloads`() = runTest {
        val note = Note(1, "title", "content", null, 1L, 1L)
        coEvery { getNotes() } returns flow { emit(listOf(note)) }

        viewModel = NotesListViewModel(getNotes, deleteNote)

        viewModel.effect.test {
            viewModel.onEvent(NoteListEvent.ConfirmDelete(1))

            assertEquals(NoteListEffect.ShowToast("Note deleted successfully"), awaitItem())
        }

        coVerify { deleteNote(note) }
    }

    @Test
    fun `when ConfirmDelete and note not found then emits ShowToast`() = runTest {
        coEvery { getNotes() } returns flow { emit(emptyList()) }
        viewModel = NotesListViewModel(getNotes, deleteNote)

        viewModel.effect.test {
            viewModel.onEvent(NoteListEvent.ConfirmDelete(99))
            assertEquals(NoteListEffect.ShowToast("Note not found"), awaitItem())
        }

        coVerify(exactly = 0) { deleteNote(any()) }
    }

    @Test
    fun `when ConfirmDelete fails then emits error toast`() = runTest {
        val note = Note(1, "title", "content", null, 1L, 1L)
        coEvery { getNotes() } returns flow { emit(listOf(note)) }
        coEvery { deleteNote(note) } throws RuntimeException("Delete failed")

        viewModel = NotesListViewModel(getNotes, deleteNote)

        viewModel.effect.test {
            viewModel.onEvent(NoteListEvent.ConfirmDelete(1))
            val effect = awaitItem()
            assertTrue((effect as NoteListEffect.ShowToast).text.contains("Failed to delete note"))
        }
    }
}
