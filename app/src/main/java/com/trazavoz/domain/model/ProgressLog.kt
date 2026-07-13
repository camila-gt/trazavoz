package com.trazavoz.domain.model

data class ProgressLog(
    val id: Int,
    val wordId: Int,
    val errorsCount: Int,
    val isCompleted: Boolean,
    val timestamp: Long
)
