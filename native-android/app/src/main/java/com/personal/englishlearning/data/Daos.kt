package com.personal.englishlearning.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(word: WordEntity): Long

    @Delete
    suspend fun delete(word: WordEntity)
}

@Dao
interface StudyEventDao {
    @Query("SELECT * FROM study_events ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<StudyEventEntity>>

    @Insert
    suspend fun insert(event: StudyEventEntity): Long
}
