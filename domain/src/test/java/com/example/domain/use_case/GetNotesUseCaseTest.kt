import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import com.example.domain.use_case.GetNotesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


@ExperimentalCoroutinesApi
class GetNotesUseCaseTest {

    private lateinit var repository: NoteRepository
    private lateinit var getNotesUseCase: GetNotesUseCase

    @Before
    fun setup() {
        repository = mock(NoteRepository::class.java)
        getNotesUseCase = GetNotesUseCase(repository)
    }

    @Test
    fun `should return empty list when no notes exist`() = runTest {
        // Arrange
        `when`(repository.getAllNotes()).thenReturn(flowOf(emptyList()))

        // Act
        val result = getNotesUseCase().first()

        // Assert
        assertTrue(result.isEmpty())
        verify(repository).getAllNotes()
    }

    @Test
    fun `should return list of notes when notes exist`() = runTest {
        // Arrange
        val notes = listOf(
            Note(1, "Note 1", "Content 1", null, 1000L, 2000L),
            Note(2, "Note 2", "Content 2", null, 2000L, 3000L)
        )
        `when`(repository.getAllNotes()).thenReturn(flowOf(notes))

        // Act
        val result = getNotesUseCase().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Note 1", result[0].title)
        assertEquals("Note 2", result[1].title)
        verify(repository).getAllNotes()
    }

    @Test
    fun `should propagate exception when repository throws`() = runTest {
        // Arrange
        val exception = RuntimeException("Database failure")
        `when`(repository.getAllNotes()).thenThrow(exception)

        // Act & Assert
        try {
            getNotesUseCase().first()
            fail("Exception was expected but not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Database failure", e.message)
        }

        verify(repository).getAllNotes()
    }
}