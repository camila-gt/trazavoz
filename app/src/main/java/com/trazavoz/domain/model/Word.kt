package com.trazavoz.domain.model

data class Word(
    val id: Int,
    val text: String,
    val initialLetter: String,
    val associatedSyllable: String,
    val syllables: List<String>,
    val imageUrl: String,
    val localImagePath: String?
)
