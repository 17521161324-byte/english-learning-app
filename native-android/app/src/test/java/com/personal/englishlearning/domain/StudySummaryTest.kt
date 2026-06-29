package com.personal.englishlearning.domain

import com.personal.englishlearning.data.StudyEventEntity
import com.personal.englishlearning.data.StudyEventType
import org.junit.Assert.assertEquals
import org.junit.Test

class StudySummaryTest {
    @Test
    fun summarizesStudyEventsByTypeAndDuration() {
        val events = listOf(
            StudyEventEntity(eventType = StudyEventType.WORD_ADDED, durationSeconds = 10),
            StudyEventEntity(eventType = StudyEventType.REVIEW, durationSeconds = 20),
            StudyEventEntity(eventType = StudyEventType.REVIEW, durationSeconds = 25),
            StudyEventEntity(eventType = StudyEventType.SPEAKING, durationSeconds = 30),
        )

        val summary = summarizeStudy(events)

        assertEquals(4, summary.totalEvents)
        assertEquals(1, summary.addedWords)
        assertEquals(2, summary.reviews)
        assertEquals(1, summary.speakingSessions)
        assertEquals(85, summary.studySeconds)
    }

    @Test
    fun ignoresNegativeDurations() {
        val summary = summarizeStudy(
            listOf(StudyEventEntity(eventType = StudyEventType.REVIEW, durationSeconds = -10)),
        )

        assertEquals(0, summary.studySeconds)
    }
}
