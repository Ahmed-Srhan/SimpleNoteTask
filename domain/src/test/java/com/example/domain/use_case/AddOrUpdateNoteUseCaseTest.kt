import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import com.example.domain.use_case.AddOrUpdateNoteUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@ExperimentalCoroutinesApi
class AddOrUpdateNoteUseCaseTest {

    private lateinit var repo: NoteRepository
    private lateinit var useCase: AddOrUpdateNoteUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        useCase = AddOrUpdateNoteUseCase(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should throw exception when title and content are empty`() = runTest {
        val note = Note(
            id = 1,
            title = null,
            content = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            useCase(note)
        }

        assertEquals("Note must have at least a title or content", exception.message)
        coVerify(exactly = 0) { repo.addOrUpdateNote(any()) }
    }

    @Test
    fun `should add note when only title is provided`() = runTest {
        val note = Note(
            id = 1,
            title = "Title Only",
            content = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        useCase(note)

        coVerify { repo.addOrUpdateNote(note) }
    }

    @Test
    fun `should add note when only content is provided`() = runTest {
        val note = Note(
            id = 1,
            title = null,
            content = "Content Only",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        useCase(note)

        coVerify { repo.addOrUpdateNote(note) }
    }

    @Test
    fun `should add note when both title and content are provided`() = runTest {
        val note = Note(
            id = 1,
            title = "Some Title",
            content = "Some Content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        useCase(note)

        coVerify { repo.addOrUpdateNote(note) }
    }
}

