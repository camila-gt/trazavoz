package com.trazavoz.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [
        Index(value = ["text"], unique = true),
        Index(value = ["initialLetter"]),
        Index(value = ["associatedSyllable"])
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val initialLetter: String,
    val associatedSyllable: String,
    val syllables: List<String>,
    val imageUrl: String,
    val localImagePath: String? = null
)
