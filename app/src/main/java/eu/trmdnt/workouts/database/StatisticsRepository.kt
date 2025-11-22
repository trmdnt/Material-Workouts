package eu.trmdnt.workouts.database

import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate

class StatisticsRepository(private val dao: StatisticsDao) {
    fun getWeightPerRepHistory(
        exerciseTemplateId: Long, start: Long = 0L, end: Long = Long.MAX_VALUE
    ): List<WeightOnDate> = dao.getWeightPerRepHistory(exerciseTemplateId, start, end)

    fun getExerciseHistory(exerciseTemplateId: Long, dayTimeStamp: Long) =
        dao.getExerciseHistoryOnDay(exerciseTemplateId, dayTimeStamp)
}