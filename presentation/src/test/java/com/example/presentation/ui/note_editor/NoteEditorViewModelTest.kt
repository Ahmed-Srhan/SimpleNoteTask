import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.domain.model.Note
import com.example.domain.use_case.AddOrUpdateNoteUseCase
import com.example.domain.use_case.DeleteNoteUseCase
import com.example.domain.use_case.GetNoteByIdUseCase
import com.example.domain.use_case.SaveImageUseCase
import com.example.presentation.ui.note_editor.NoteEditorEffect
import com.example.presentation.ui.note_editor.NoteEditorEvent
import com.example.presentation.ui.note_editor.NoteEditorViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {

    private val getNoteById: GetNoteByIdUseCase = mockk()
    private val addOrUpdateNote: AddOrUpdateNoteUseCase = mockk()
    private val deleteNote: DeleteNoteUseCase = mockk()
    private val saveImage: SaveImageUseCase = mockk()
    private lateinit var viewModel: NoteEditorViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createVM(noteId: Int? = null): NoteEditorViewModel {
        val handle = SavedStateHandle().apply {
            noteId?.let { set("noteId", it) }
        }
        return NoteEditorViewModel(getNoteById, addOrUpdateNote, deleteNote, saveImage, handle)
    }

    @Test
    fun `load existing note from id`() = runTest {
        val note = Note(1, "title", "content", null, 1, 1)
        coEvery { getNoteById(1) } returns note

        viewModel = createVM(1)
        advanceUntilIdle()

        assertEquals(note, viewModel.state.value.note)
    }

    @Test
    fun `load empty note when no id`() = runTest {
        viewModel = createVM()
        advanceUntilIdle()

        val note = viewModel.state.value.note
        assertEquals(0, note.id)
        assertTrue(note.content.isEmpty())
    }

    @Test
    fun `update title updates state`() = runTest {
        viewModel = createVM()
        viewModel.onEvent(NoteEditorEvent.TitleChanged("new title"))
        assertEquals("new title", viewModel.state.value.note.title)
    }

    @Test
    fun `update content updates state`() = runTest {
        viewModel = createVM()
        viewModel.onEvent(NoteEditorEvent.ContentChanged("new content"))
        assertEquals("new content", viewModel.state.value.note.content)
    }

    @Test
    fun `clear image sets imageUri null`() = runTest {
        val note = Note(1, "t", "c", "img", 1, 1)
        coEvery { getNoteById(1) } returns note
        viewModel = createVM(1)
        advanceUntilIdle()

        viewModel.onEvent(NoteEditorEvent.ClearImage)
        assertNull(viewModel.state.value.note.imageUri)
    }

    @Test
    fun `attach image success updates note`() = runTest {
        val uri = mockk<Uri>()
        coEvery { saveImage(uri) } returns "path.jpg"

        viewModel = createVM()
        viewModel.onEvent(NoteEditorEvent.AttachImageUri(uri))
        advanceUntilIdle()

        assertEquals("path.jpg", viewModel.state.value.note.imageUri)
    }

    @Test
    fun `attach image failure emits toast`() = runTest {
        val uri = mockk<Uri>()
        coEvery { saveImage(uri) } throws RuntimeException("fail")

        viewModel = createVM()

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.AttachImageUri(uri))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is NoteEditorEffect.ShowToast)
            assertTrue((effect as NoteEditorEffect.ShowToast).text.contains("fail"))
        }
    }

    @Test
    fun `save empty note shows toast`() = runTest {
        coEvery { addOrUpdateNote(any()) } throws IllegalArgumentException("Note must have at least a title or content")
        viewModel = createVM()

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Save)
            advanceUntilIdle()

            val effect = awaitItem()
            assertEquals(
                "Note must have at least a title or content",
                (effect as NoteEditorEffect.ShowToast).text
            )
        }
    }

    @Test
    fun `save valid note success emits back`() = runTest {
        coEvery { addOrUpdateNote(any()) } just Runs
        viewModel = createVM()

        viewModel.onEvent(NoteEditorEvent.TitleChanged("t"))
        viewModel.onEvent(NoteEditorEvent.ContentChanged("c"))

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Save)
            advanceUntilIdle()

            assertEquals(NoteEditorEffect.BackToList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save invalid note emits toast`() = runTest {
        coEvery { addOrUpdateNote(any()) } throws IllegalArgumentException("bad")
        viewModel = createVM()

        // note is valid, but mocked use case will throw IllegalArgumentException
        viewModel.onEvent(NoteEditorEvent.TitleChanged("t"))
        viewModel.onEvent(NoteEditorEvent.ContentChanged("c"))

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Save)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is NoteEditorEffect.ShowToast)
            assertEquals("bad", (effect as NoteEditorEffect.ShowToast).text)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save general failure emits toast`() = runTest {
        coEvery { addOrUpdateNote(any()) } throws RuntimeException("boom")
        viewModel = createVM()

        viewModel.onEvent(NoteEditorEvent.TitleChanged("t"))
        viewModel.onEvent(NoteEditorEvent.ContentChanged("c"))

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Save)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is NoteEditorEffect.ShowToast)
            assertTrue((effect as NoteEditorEffect.ShowToast).text.contains("Save failed"))

            cancelAndIgnoreRemainingEvents()
        }
    }


    // 5. Delete
    @Test
    fun `delete note with id 0 shows toast`() = runTest {
        viewModel = createVM()
        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Delete)
            advanceUntilIdle()
            val effect = awaitItem()
            assertEquals("No note to delete", (effect as NoteEditorEffect.ShowToast).text)
        }
    }

    @Test
    fun `delete existing note success emits back`() = runTest {
        val note = Note(1, "t", "c", null, 1, 1)
        coEvery { getNoteById(1) } returns note
        coEvery { deleteNote(note) } just Runs

        viewModel = createVM(1)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Delete)
            advanceUntilIdle()
            assertEquals(NoteEditorEffect.BackToList, awaitItem())
        }
    }

    @Test
    fun `delete failure emits toast`() = runTest {
        val note = Note(1, "t", "c", null, 1, 1)
        coEvery { getNoteById(1) } returns note
        coEvery { deleteNote(note) } throws RuntimeException("err")

        viewModel = createVM(1)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(NoteEditorEvent.Delete)
            advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is NoteEditorEffect.ShowToast)
            assertTrue((effect as NoteEditorEffect.ShowToast).text.contains("Delete failed"))
        }
    }
}
