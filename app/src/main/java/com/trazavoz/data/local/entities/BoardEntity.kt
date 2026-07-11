package com.trazavoz.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "boards",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class BoardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
