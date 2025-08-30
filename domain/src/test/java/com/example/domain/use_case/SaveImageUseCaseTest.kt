import android.net.Uri
import com.example.domain.repo.NoteRepository
import com.example.domain.use_case.SaveImageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


@OptIn(ExperimentalCoroutinesApi::class)
class SaveImageUseCaseTest {

    private lateinit var repository: NoteRepository
    private lateinit var saveImageUseCase: SaveImageUseCase

    @Before
    fun setup() {
        repository = mock()
        saveImageUseCase = SaveImageUseCase(repository)
    }

    @Test
    fun `invoke should return saved image path`() = runTest {
        // Arrange
        val uri = mock<Uri>()
        val expectedPath = "images/note_123.png"
        `when`(repository.saveImage(uri)).thenReturn(expectedPath)

        // Act
        val result = saveImageUseCase(uri)

        // Assert
        assertEquals(expectedPath, result)
        verify(repository).saveImage(uri)
    }
}