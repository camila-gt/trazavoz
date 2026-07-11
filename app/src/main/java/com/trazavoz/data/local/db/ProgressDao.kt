package com.trazavoz.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.trazavoz.data.local.entities.ProgressLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert
    suspend fun insertLog(log: ProgressLogEntity)

    @Query("SELECT * FROM progress_logs WHERE wordId = :wordId ORDER BY timestamp DESC")
    fun getLogsForWord(wordId: Int): Flow<List<ProgressLogEntity>>

    @Query("SELECT * FROM progress_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<ProgressLogEntity>>
}
