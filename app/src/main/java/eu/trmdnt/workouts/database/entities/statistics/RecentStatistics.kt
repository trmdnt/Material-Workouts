package eu.trmdnt.workouts.database.entities.statistics

data class RecentStatistics(
    val workoutCountWeek: Int,
    val workoutCountMonth: Int,
    val workoutCountYear: Int,
    val workoutCountTotal: Int,
    val timeWorkingOutWeek: Long,
    val timeWorkingOutMonth: Long,
    val timeWorkingOutYear: Long,
    val timeWorkingOutTotal: Long,
)
