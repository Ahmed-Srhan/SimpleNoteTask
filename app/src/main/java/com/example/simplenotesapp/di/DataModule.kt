package com.example.simplenotesapp.di

import android.content.Context
import androidx.room.Room
import com.example.data.datasource.local.dao.NoteDao
import com.example.data.datasource.local.db.NoteDatabase
import com.example.data.datasource.storage.FileStorageManager
import com.example.data.repo.NoteRepositoryImpl
import com.example.domain.repo.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoteDatabase =
        Room.databaseBuilder(context, NoteDatabase::class.java, "notes_db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideNoteDao(db: NoteDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideFileStorageManager(@ApplicationContext context: Context): FileStorageManager =
        FileStorageManager(context)

    @Provides
    @Singleton
    fun provideNoteRepository(
        dao: NoteDao, storageManager: FileStorageManager
    ): NoteRepository = NoteRepositoryImpl(dao, storageManager)
}