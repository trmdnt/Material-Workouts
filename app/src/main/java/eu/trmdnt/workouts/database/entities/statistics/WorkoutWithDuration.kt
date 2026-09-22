package eu.trmdnt.workouts.database.entities.statistics

import androidx.room3.Embedded
import eu.trmdnt.workouts.database.entities.Workout

data class WorkoutWithDuration(
    @Embedded val workout: Workout,
    val timeSpentSeconds: Long
)
