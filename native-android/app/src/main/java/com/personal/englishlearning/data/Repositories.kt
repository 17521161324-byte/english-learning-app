package com.personal.englishlearning.data

import kotlinx.coroutines.flow.Flow

class WordRepository(private val dao: WordDao) {
    val words: Flow<List<WordEntity>> = dao.observeAll()

    suspend fun add(term: String, meaning: String, note: String): Result<Long> = runCatching {
        dao.insert(
            WordEntity(
                term = term.trim().lowercase(),
                meaning = meaning.trim(),
                note = note.trim(),
            ),
        )
    }

    suspend fun delete(word: WordEntity) = dao.delete(word)
}

class StudyRepository(private val dao: StudyEventDao) {
    val events: Flow<List<StudyEventEntity>> = dao.observeAll()

    suspend fun recordWordAdded(wordId: Long) {
        dao.insert(StudyEventEntity(eventType = StudyEventType.WORD_ADDED, itemId = wordId))
    }
}
