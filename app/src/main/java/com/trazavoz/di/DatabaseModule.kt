package com.trazavoz.di

import android.content.Context
import androidx.room.Room
import com.trazavoz.data.local.db.AppDatabase
import com.trazavoz.data.local.db.BoardDao
import com.trazavoz.data.local.db.ProgressDao
import com.trazavoz.data.local.db.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "trazavoz_database"
        ).build()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    fun provideBoardDao(database: AppDatabase): BoardDao {
        return database.boardDao()
    }

    @Provides
    fun provideProgressDao(database: AppDatabase): ProgressDao {
        return database.progressDao()
    }
}
