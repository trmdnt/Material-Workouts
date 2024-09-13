package com.example.gymutil.di

import android.content.Context
import androidx.room.Room
import com.example.gymutil.database.AppDatabase
import com.example.gymutil.database.Dao
import com.example.gymutil.database.GymRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "gym_database"
        ).build()
    }

    @Provides
    fun provideDao(database: AppDatabase): Dao {
        return database.dao()
    }

    @Provides
    @Singleton
    fun provideGymRepository(dao: Dao): GymRepository {
        return GymRepository(dao)
    }
}