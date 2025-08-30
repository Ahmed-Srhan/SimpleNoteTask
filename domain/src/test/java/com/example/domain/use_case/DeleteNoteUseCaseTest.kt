package com.example.domain.use_case

import com.example.domain.model.Note
import com.example.domain.repo.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class DeleteNoteUseCaseTest {

    private lateinit var repo: NoteRepository
    private lateinit var useCase: DeleteNoteUseCase

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)
        useCase = DeleteNoteUseCase(repo)
    }

    @Test
    fun `should call repo deleteNote`() = runTest {
        val note = Note(
            id = 1,
            title = "Test",
            content = "Some content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { repo.deleteNote(note) } returns Unit

        useCase(note)

        coVerify(exactly = 1) { repo.deleteNote(note) }
    }
}
