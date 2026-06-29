package com.personal.englishlearning.domain

import com.personal.englishlearning.data.StudyEventEntity
import com.personal.englishlearning.data.StudyEventType

data class StudySummary(
    val totalEvents: Int,
    val addedWords: Int,
    val reviews: Int,
    val speakingSessions: Int,
    val studySeconds: Int,
)

fun summarizeStudy(events: List<StudyEventEntity>): StudySummary = StudySummary(
    totalEvents = events.size,
    addedWords = events.count { it.eventType == StudyEventType.WORD_ADDED },
    reviews = events.count { it.eventType == StudyEventType.REVIEW },
    speakingSessions = events.count { it.eventType == StudyEventType.SPEAKING },
    studySeconds = events.sumOf { it.durationSeconds.coerceAtLeast(0) },
)
