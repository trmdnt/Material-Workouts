package eu.trmdnt.workouts.database.entities

import androidx.room.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = arrayOf("exercise_id"),
            childColumns = arrayOf("exercise_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    tableName = "Set",
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    @ColumnInfo
    val time: Long = 0,

    @ColumnInfo
    val reps: Int = 0,

    @ColumnInfo
    val weight: Double = 0.0,

    @ColumnInfo
    val distance: Double = 0.0,

    @ColumnInfo
    val date: Long
)

enum class SetProperty {
    TIME,
    REPS,
    WEIGHT,
    DISTANCE
}
