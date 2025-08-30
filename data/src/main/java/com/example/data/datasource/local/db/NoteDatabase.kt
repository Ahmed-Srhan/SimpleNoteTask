package com.example.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.datasource.local.dao.NoteDao
import com.example.data.datasource.local.entity.NoteEntity


@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
