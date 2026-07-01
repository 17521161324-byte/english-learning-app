package com.personal.englishlearning.domain

import com.personal.englishlearning.data.LearningSettings
import com.personal.englishlearning.data.StudyEventEntity
import com.personal.englishlearning.data.StudyEventType
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayPlanTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val now = ZonedDateTime.of(2026, 7, 1, 12, 0, 0, 0, zone).toInstant().toEpochMilli()

    @Test
    fun onlyCountsTodayEventsAndActiveWords() {
        val yesterday = now - 24 * 60 * 60 * 1000L
        val plan = buildTodayPlan(
            events = listOf(
                event(StudyEventType.WORD_ADDED, itemId = 1),
                event(StudyEventType.WORD_ADDED, itemId = 1),
                event(StudyEventType.WORD_ADDED, itemId = 2),
                event(StudyEventType.WORD_ADDED, itemId = 3, occurredAt = yesterday),
            ),
            settings = LearningSettings(dailyNewWords = 2),
            activeWordIds = setOf(1, 3),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(1, plan.newWords.current)
        assertEquals(PlanTaskStatus.IN_PROGRESS, plan.newWords.status)
    }

    @Test
    fun calculatesAllTaskProgressFromStudyEvents() {
        val plan = buildTodayPlan(
            events = listOf(
                event(StudyEventType.WORD_ADDED, itemId = 1, duration = 120),
                event(StudyEventType.REVIEW, itemId = 1, duration = 60),
                event(StudyEventType.REVIEW, itemId = 1, duration = 60),
                event(StudyEventType.REVIEW, itemId = 2, duration = 60),
                event(StudyEventType.SPEAKING, duration = 300),
            ),
            settings = LearningSettings(
                dailyNewWords = 1,
                dailyReviewWords = 2,
                dailySpeakingSessions = 1,
                dailyMinutes = 10,
            ),
            activeWordIds = setOf(1),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(PlanTaskStatus.COMPLETED, plan.newWords.status)
        assertEquals(2, plan.reviews.current)
        assertEquals(1, plan.speaking.current)
        assertEquals(10, plan.minutes.current)
        assertEquals(100, plan.completionPercent)
    }

    @Test
    fun disabledGoalsDoNotLowerCompletion() {
        val plan = buildTodayPlan(
            events = listOf(event(StudyEventType.WORD_ADDED, itemId = 1)),
            settings = LearningSettings(
                dailyNewWords = 1,
                dailyReviewWords = 0,
                dailySpeakingSessions = 0,
                dailyMinutes = 5,
            ),
            activeWordIds = setOf(1),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(50, plan.completionPercent)
        assertEquals(PlanTaskStatus.DISABLED, plan.reviews.status)
    }

    private fun event(
        type: String,
        itemId: Long? = null,
        duration: Int = 0,
        occurredAt: Long = now,
    ) = StudyEventEntity(
        eventType = type,
        itemId = itemId,
        durationSeconds = duration,
        occurredAt = occurredAt,
    )
}
