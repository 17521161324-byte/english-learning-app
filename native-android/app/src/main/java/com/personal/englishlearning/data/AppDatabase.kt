package com.personal.englishlearning.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class, StudyEventEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun studyEventDao(): StudyEventDao
}
