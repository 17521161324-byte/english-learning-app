package com.personal.englishlearning

import android.app.Application
import androidx.room.Room
import com.personal.englishlearning.data.AppDatabase
import com.personal.englishlearning.data.SettingsRepository
import com.personal.englishlearning.data.StudyRepository
import com.personal.englishlearning.data.UpdateRepository
import com.personal.englishlearning.data.WordRepository

class EnglishLearningApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "english-learning.db",
        ).build()
        container = AppContainer(
            words = WordRepository(database.wordDao()),
            study = StudyRepository(database.studyEventDao()),
            settings = SettingsRepository(applicationContext),
            updates = UpdateRepository(),
        )
    }
}

data class AppContainer(
    val words: WordRepository,
    val study: StudyRepository,
    val settings: SettingsRepository,
    val updates: UpdateRepository,
)
