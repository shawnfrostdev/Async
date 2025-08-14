package com.shawnfrost.async.di

import android.content.Context
import androidx.room.Room
import com.shawnfrost.async.data.local.AsyncDatabase
import com.shawnfrost.async.data.local.dao.PlaylistDao
import com.shawnfrost.async.data.local.dao.SearchHistoryDao
import com.shawnfrost.async.data.local.dao.TrackDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AsyncDatabase {
        return Room.databaseBuilder(
            context,
            AsyncDatabase::class.java,
            AsyncDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // For development - clears DB on schema changes
        .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AsyncDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: AsyncDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AsyncDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }
} 