package com.trazavoz.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trazavoz.data.local.entities.BoardEntity
import com.trazavoz.data.local.entities.ProgressLogEntity
import com.trazavoz.data.local.entities.WordBoardCrossRef
import com.trazavoz.data.local.entities.WordEntity

@Database(
    entities = [
        WordEntity::class,
        BoardEntity::class,
        WordBoardCrossRef::class,
        ProgressLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun boardDao(): BoardDao
    abstract fun progressDao(): ProgressDao
}
