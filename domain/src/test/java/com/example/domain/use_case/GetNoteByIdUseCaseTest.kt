import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import com.example.domain.use_case.GetNoteByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class GetNoteByIdUseCaseTest {

    private lateinit var repo: NoteRepository
    private lateinit var getNoteByIdUseCase: GetNoteByIdUseCase

    @Before
    fun setUp() {
        repo = mockk()
        getNoteByIdUseCase = GetNoteByIdUseCase(repo)
    }

    @Test
    fun `when note exists, should return note`() = runTest {
        // Given
        val note = Note(
            id = 1,
            title = "Test Note",
            content = "Some content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        coEvery { repo.getNoteById(1) } returns note

        // When
        val result = getNoteByIdUseCase(1)

        // Then
        assertNotNull(result)
        assertEquals(note, result)
        coVerify(exactly = 1) { repo.getNoteById(1) }
    }

    @Test
    fun `when note does not exist, should return null`() = runTest {
        // Given
        coEvery { repo.getNoteById(99) } returns null

        // When
        val result = getNoteByIdUseCase(99)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { repo.getNoteById(99) }
    }

    @Test(expected = Exception::class)
    fun `when repository throws exception, should rethrow`() = runTest {
        // Given
        coEvery { repo.getNoteById(5) } throws Exception("DB error")

        // When
        getNoteByIdUseCase(5) // should throw
    }
}
