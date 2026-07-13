package com.trazavoz.data.repository

import com.trazavoz.data.local.db.ProgressDao
import com.trazavoz.data.local.entities.ProgressLogEntity
import com.trazavoz.domain.model.ProgressLog
import com.trazavoz.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao
) : ProgressRepository {

    override suspend fun insertLog(wordId: Int, errorsCount: Int, isCompleted: Boolean) {
        val entity = ProgressLogEntity(
            wordId = wordId,
            errorsCount = errorsCount,
            isCompleted = isCompleted,
            timestamp = System.currentTimeMillis()
        )
        progressDao.insertLog(entity)
    }

    override fun getLogsForWord(wordId: Int): Flow<List<ProgressLog>> =
        progressDao.getLogsForWord(wordId).map { list -> list.map { it.toDomain() } }

    override fun getAllLogs(): Flow<List<ProgressLog>> =
        progressDao.getAllLogsFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun clearAllLogs() {
        progressDao.deleteAllLogs()
    }
}

fun ProgressLogEntity.toDomain() = ProgressLog(id, wordId, errorsCount, isCompleted, timestamp)
