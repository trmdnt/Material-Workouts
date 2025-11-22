package eu.trmdnt.workouts.database.entities.statistics

data class WeightOnDate(
    var date: String,
    var totalWeight: Double,
    var totalReps: Double,
    var weightPerRep: Double,
    var dateTimeStamp: Long,
)