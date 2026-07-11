package com.trazavoz.domain.usecase

import com.trazavoz.domain.repository.ProgressRepository
import javax.inject.Inject

class ClearProgressStatsUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke() {
        progressRepository.clearAllLogs()
    }
}
