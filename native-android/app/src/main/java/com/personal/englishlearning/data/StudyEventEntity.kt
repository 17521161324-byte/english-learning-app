package com.personal.englishlearning.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_events",
    indices = [Index("occurredAt"), Index("eventType")],
)
data class StudyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val itemId: Long? = null,
    val durationSeconds: Int = 0,
    val result: String? = null,
    val occurredAt: Long = System.currentTimeMillis(),
)

object StudyEventType {
    const val WORD_ADDED = "word_added"
    const val REVIEW = "review"
    const val SPEAKING = "speaking"
}
