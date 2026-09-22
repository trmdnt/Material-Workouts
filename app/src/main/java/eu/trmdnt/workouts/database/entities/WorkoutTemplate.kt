package eu.trmdnt.workouts.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "workout_template")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "workout_template_id")
    val workoutTemplateId: Long = 0,

    @ColumnInfo
    val name: String,

    @ColumnInfo(name = "last_used")
    val lastUsed: Int? = null
)
