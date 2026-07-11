package com.trazavoz.domain.usecase

import com.trazavoz.data.remote.CachingManager
import com.trazavoz.domain.model.Word
import com.trazavoz.domain.repository.WordRepository
import javax.inject.Inject

class SaveWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val cachingManager: CachingManager
) {
    suspend operator fun invoke(word: Word, arasaacId: Int): Boolean {
        val localPath = cachingManager.downloadAndCacheImage(arasaacId)
        val wordToSave = word.copy(localImagePath = localPath)
        val resultId = wordRepository.insertWord(wordToSave)
        return resultId > 0
    }
}
