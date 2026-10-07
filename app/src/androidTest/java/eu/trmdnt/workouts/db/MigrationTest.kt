package eu.trmdnt.workouts.db

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eu.trmdnt.workouts.database.AppDatabase
import eu.trmdnt.workouts.database.MIGRATION_3_4
import eu.trmdnt.workouts.database.MIGRATION_4_5
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = instrumentation,
        databaseClass = AppDatabase::class,
        driver = AndroidSQLiteDriver(),
        file = instrumentation.targetContext.getDatabasePath(TEST_DB),
    )

    @Test
    fun migrateAll() = runTest {
        val connection = helper.createDatabase(1)
        connection.close()

        val db = Room.databaseBuilder<AppDatabase>(instrumentation.targetContext, TEST_DB)
            .setDriver(AndroidSQLiteDriver())
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
            .build()

        db.useReaderConnection { connection ->

        }

        db.close()
    }
}