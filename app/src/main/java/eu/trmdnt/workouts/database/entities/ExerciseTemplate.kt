package eu.trmdnt.workouts.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(
    tableName = "exercise_template",
)
data class ExerciseTemplate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exercise_template_id")
    val exerciseTemplateId: Long = 0,

    @ColumnInfo
    val name: String,

    @ColumnInfo
    val weight: Boolean = true,

    @ColumnInfo
    val time: Boolean = false,

    @ColumnInfo
    val reps: Boolean = true,

    @ColumnInfo
    val distance: Boolean = false,

    //TODO use this instead of deleting
    @ColumnInfo
    val hidden: Boolean = false,

    @ColumnInfo(name = "weight_times_two", defaultValue = "false")
    val weightTimesTwo: Boolean = false,
)
