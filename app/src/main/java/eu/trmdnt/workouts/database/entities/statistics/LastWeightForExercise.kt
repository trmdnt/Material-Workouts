package eu.trmdnt.workouts.database.entities.statistics

import eu.trmdnt.workouts.database.entities.SetType

data class LastWeightForExercise(
    val default: Double,
    val drop: Double,
    val warmup: Double
) {
    fun chooseWeight(setType: SetType): Double {
        return when (setType) {
            SetType.Default -> default
            SetType.WarmUp -> warmup
            SetType.Drop -> drop
        }
    }
}