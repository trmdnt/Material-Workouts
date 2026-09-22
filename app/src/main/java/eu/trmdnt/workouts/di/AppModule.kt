package eu.trmdnt.workouts.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.trmdnt.workouts.database.AppDatabase
import eu.trmdnt.workouts.database.Dao
import eu.trmdnt.workouts.database.GymRepository
import eu.trmdnt.workouts.database.StatisticsDao
import eu.trmdnt.workouts.database.StatisticsRepository
import eu.trmdnt.workouts.database.getDatabase
import eu.trmdnt.workouts.service.TimerServiceManager
import eu.trmdnt.workouts.settings.SettingsManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return getDatabase(context)
    }

    @Provides
    fun provideDao(database: AppDatabase): Dao {
        return database.dao()
    }

    @Provides
    fun provideStatisticsDao(database: AppDatabase): StatisticsDao {
        return database.statisticsDao()
    }

    @Provides
    @Singleton
    fun provideGymRepository(dao: Dao): GymRepository {
        return GymRepository(dao)
    }

    @Provides
    @Singleton
    fun provideStatisticsRepository(dao: StatisticsDao): StatisticsRepository {
        return StatisticsRepository(dao)
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                appContext.preferencesDataStoreFile("preferences")
            }
        )

    @Provides
    @Singleton
    fun provideSettingsManager(preferencesDataStore: DataStore<Preferences>): SettingsManager {
        return SettingsManager(preferencesDataStore)
    }

    @Provides
    @Singleton
    fun provideTimerServiceManager(
        @ApplicationContext appContext: Context,
        settingsManager: SettingsManager
    ): TimerServiceManager {
        return TimerServiceManager(appContext, settingsManager)
    }
}