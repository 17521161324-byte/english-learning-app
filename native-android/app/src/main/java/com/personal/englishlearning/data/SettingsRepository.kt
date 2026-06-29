package com.personal.englishlearning.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "learning_settings")

data class LearningSettings(
    val dailyNewWords: Int = 10,
    val dailyReviewWords: Int = 20,
    val dailyMinutes: Int = 30,
    val remindersEnabled: Boolean = false,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val dailyNewWords = intPreferencesKey("daily_new_words")
        val dailyReviewWords = intPreferencesKey("daily_review_words")
        val dailyMinutes = intPreferencesKey("daily_minutes")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
    }

    val settings: Flow<LearningSettings> = context.settingsDataStore.data.map { values ->
        LearningSettings(
            dailyNewWords = values[Keys.dailyNewWords] ?: 10,
            dailyReviewWords = values[Keys.dailyReviewWords] ?: 20,
            dailyMinutes = values[Keys.dailyMinutes] ?: 30,
            remindersEnabled = values[Keys.remindersEnabled] ?: false,
        )
    }

    suspend fun updateGoals(newWords: Int, reviewWords: Int, minutes: Int) {
        context.settingsDataStore.edit { values ->
            values[Keys.dailyNewWords] = newWords.coerceIn(0, 100)
            values[Keys.dailyReviewWords] = reviewWords.coerceIn(0, 200)
            values[Keys.dailyMinutes] = minutes.coerceIn(5, 240)
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.remindersEnabled] = enabled }
    }
}
