package com.example.simplenotesapp.di

import com.example.domain.repo.NoteRepository
import com.example.domain.use_case.AddOrUpdateNoteUseCase
import com.example.domain.use_case.DeleteNoteUseCase
import com.example.domain.use_case.GetNoteByIdUseCase
import com.example.domain.use_case.GetNotesUseCase
import com.example.domain.use_case.SaveImageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetNotesUseCase(repo: NoteRepository): GetNotesUseCase {
        return GetNotesUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideGetNoteByIdUseCase(repo: NoteRepository): GetNoteByIdUseCase {
        return GetNoteByIdUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideAddOrUpdateNoteUseCase(repo: NoteRepository): AddOrUpdateNoteUseCase {
        return AddOrUpdateNoteUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideDeleteNoteUseCase(repo: NoteRepository): DeleteNoteUseCase {
        return DeleteNoteUseCase(repo)
    }

    @Provides
    @Singleton
    fun provideSaveImageUseCase(repo: NoteRepository): SaveImageUseCase {
        return SaveImageUseCase(repo)
    }

}