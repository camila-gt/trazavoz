package com.trazavoz.domain.repository

import com.trazavoz.domain.model.ProgressLog
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    suspend fun insertLog(wordId: Int, errorsCount: Int, isCompleted: Boolean)
    fun getLogsForWord(wordId: Int): Flow<List<ProgressLog>>
    fun getAllLogs(): Flow<List<ProgressLog>>
}
