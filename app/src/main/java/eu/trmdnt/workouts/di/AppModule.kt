package eu.trmdnt.workouts.di

import android.app.Application
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
import eu.trmdnt.workouts.database.*
import eu.trmdnt.workouts.database.backup.BackupManager
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

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext appContext: Context,
        database: AppDatabase,
    ): BackupManager {
        return BackupManager(database, appContext)
    }

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}