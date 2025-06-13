package eu.trmdnt.workouts.database

import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate

class StatisticsRepository(private val dao: StatisticsDao) {
    fun getWeightPerRepHistory(exerciseId: Long, start: Long = 0L, end: Long = Long.MAX_VALUE): List<WeightOnDate> =
        dao.getWeightPerRepHistory(exerciseId, start, end)
}