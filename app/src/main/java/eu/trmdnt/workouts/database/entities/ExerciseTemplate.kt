package eu.trmdnt.workouts.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val weight: Boolean,

    @ColumnInfo
    val time: Boolean,

    @ColumnInfo
    val reps: Boolean,

    @ColumnInfo
    val distance: Boolean,

    @ColumnInfo
    val hidden: Boolean,
)
