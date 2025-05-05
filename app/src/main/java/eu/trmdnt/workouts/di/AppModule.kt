package eu.trmdnt.workouts.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import eu.trmdnt.workouts.database.AppDatabase
import eu.trmdnt.workouts.database.Dao
import eu.trmdnt.workouts.database.GymRepository
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
            context.applicationContext, AppDatabase::class.java, "gym_database"
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

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                appContext.preferencesDataStoreFile("preferences")
            }
        )
}