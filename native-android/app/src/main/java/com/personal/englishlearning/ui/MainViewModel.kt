package com.personal.englishlearning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.personal.englishlearning.AppContainer
import com.personal.englishlearning.BuildConfig
import com.personal.englishlearning.data.LearningSettings
import com.personal.englishlearning.data.StudyEventEntity
import com.personal.englishlearning.data.UpdateInfo
import com.personal.englishlearning.data.WordEntity
import com.personal.englishlearning.domain.summarizeStudy
import com.personal.englishlearning.domain.buildTodayPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UpdateUiState(
    val checking: Boolean = false,
    val message: String = "尚未检查",
    val info: UpdateInfo? = null,
)

data class MainUiState(
    val words: List<WordEntity> = emptyList(),
    val events: List<StudyEventEntity> = emptyList(),
    val settings: LearningSettings = LearningSettings(),
    val update: UpdateUiState = UpdateUiState(),
    val transientMessage: String? = null,
) {
    val summary get() = summarizeStudy(events)
    val todayPlan get() = buildTodayPlan(events, settings, words.map { it.id }.toSet())
}

class MainViewModel(private val container: AppContainer) : ViewModel() {
    private val updateState = MutableStateFlow(UpdateUiState())
    private val transientMessage = MutableStateFlow<String?>(null)

    val uiState = combine(
        container.words.words,
        container.study.events,
        container.settings.settings,
        updateState,
        transientMessage,
    ) { words, events, settings, update, message ->
        MainUiState(words, events, settings, update, message)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    fun addWord(term: String, meaning: String, note: String) {
        if (term.isBlank() || meaning.isBlank()) {
            transientMessage.value = "请填写单词和中文释义"
            return
        }
        viewModelScope.launch {
            container.words.add(term, meaning, note)
                .onSuccess { wordId ->
                    container.study.recordWordAdded(wordId)
                    transientMessage.value = "已加入生词库"
                }
                .onFailure {
                    transientMessage.value = "该单词已存在"
                }
        }
    }

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            container.words.delete(word)
            transientMessage.value = "已删除 ${word.term}"
        }
    }

    fun updateGoals(newWords: Int, reviewWords: Int, speakingSessions: Int, minutes: Int) {
        viewModelScope.launch {
            container.settings.updateGoals(newWords, reviewWords, speakingSessions, minutes)
            transientMessage.value = "学习计划已保存"
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { container.settings.setRemindersEnabled(enabled) }
    }

    fun checkForUpdate() {
        if (updateState.value.checking) return
        updateState.value = UpdateUiState(checking = true, message = "正在检查")
        viewModelScope.launch {
            container.updates.check()
                .onSuccess { info ->
                    updateState.value = UpdateUiState(
                        message = if (info.hasUpdate) "发现 ${info.versionName}" else "当前已是最新版本",
                        info = info,
                    )
                }
                .onFailure {
                    updateState.value = UpdateUiState(message = "检查失败，请确认网络连接")
                }
        }
    }

    fun consumeMessage() {
        transientMessage.value = null
    }

    companion object {
        val currentVersion: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }
}

class MainViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java))
        return MainViewModel(container) as T
    }
}
