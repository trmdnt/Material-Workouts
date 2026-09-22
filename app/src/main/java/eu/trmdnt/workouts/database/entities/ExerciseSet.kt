package eu.trmdnt.workouts.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

enum class SetType {
    Default,
    WarmUp,
    Drop
}

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

    @ColumnInfo(name = "exercise_id", index = true)
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
    val date: Long,

    @ColumnInfo(name = "set_type", defaultValue = "Default")
    val setType: SetType = SetType.Default,
)