package com.personal.englishlearning.domain

import com.personal.englishlearning.data.LearningSettings
import com.personal.englishlearning.data.StudyEventEntity
import com.personal.englishlearning.data.StudyEventType
import java.time.Instant
import java.time.ZoneId

enum class PlanTaskStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    DISABLED,
}

data class PlanTaskProgress(
    val current: Int,
    val target: Int,
) {
    val status: PlanTaskStatus
        get() = when {
            target <= 0 -> PlanTaskStatus.DISABLED
            current <= 0 -> PlanTaskStatus.NOT_STARTED
            current >= target -> PlanTaskStatus.COMPLETED
            else -> PlanTaskStatus.IN_PROGRESS
        }

    val fraction: Float
        get() = if (target <= 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)
}

data class TodayPlan(
    val newWords: PlanTaskProgress,
    val reviews: PlanTaskProgress,
    val speaking: PlanTaskProgress,
    val minutes: PlanTaskProgress,
    val studySeconds: Int,
    val completionPercent: Int,
)

fun buildTodayPlan(
    events: List<StudyEventEntity>,
    settings: LearningSettings,
    activeWordIds: Set<Long>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): TodayPlan {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val startMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val todayEvents = events.filter { it.occurredAt in startMillis until endMillis }

    val newWordCount = todayEvents
        .filter {
            it.eventType == StudyEventType.WORD_ADDED &&
                it.itemId != null &&
                it.itemId in activeWordIds
        }
        .mapNotNull { it.itemId }
        .distinct()
        .size
    val reviewCount = countDistinctItems(todayEvents.filter { it.eventType == StudyEventType.REVIEW })
    val speakingCount = todayEvents.count { it.eventType == StudyEventType.SPEAKING }
    val studySeconds = todayEvents.sumOf { it.durationSeconds.coerceAtLeast(0) }

    val tasks = listOf(
        PlanTaskProgress(newWordCount, settings.dailyNewWords),
        PlanTaskProgress(reviewCount, settings.dailyReviewWords),
        PlanTaskProgress(speakingCount, settings.dailySpeakingSessions),
        PlanTaskProgress(studySeconds / 60, settings.dailyMinutes),
    )
    val enabledTasks = tasks.filter { it.target > 0 }
    val completion = if (enabledTasks.isEmpty()) {
        100
    } else {
        (enabledTasks.sumOf { it.fraction.toDouble() } / enabledTasks.size * 100).toInt()
    }

    return TodayPlan(
        newWords = tasks[0],
        reviews = tasks[1],
        speaking = tasks[2],
        minutes = tasks[3],
        studySeconds = studySeconds,
        completionPercent = completion,
    )
}

private fun countDistinctItems(events: List<StudyEventEntity>): Int {
    val itemIds = events.mapNotNull { it.itemId }.distinct().size
    val eventsWithoutItem = events.count { it.itemId == null }
    return itemIds + eventsWithoutItem
}
