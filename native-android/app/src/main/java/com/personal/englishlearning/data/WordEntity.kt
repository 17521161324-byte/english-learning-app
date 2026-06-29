package com.personal.englishlearning.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [Index(value = ["term"], unique = true)],
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val meaning: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
