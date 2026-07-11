package com.trazavoz.domain.model

data class Board(
    val id: Int,
    val name: String,
    val words: List<Word> = emptyList()
)
