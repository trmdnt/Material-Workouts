package eu.trmdnt.workouts.database

import androidx.room.Dao
import androidx.room.Query
import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate

@Dao
interface StatisticsDao {
    @Query(
        "SELECT strftime ('%F', date, 'unixepoch') AS date, " +
                "SUM(reps * weight) AS totalWeight, " +
                "SUM(reps) AS totalReps," +
                "CAST(SUM(reps * weight) AS FLOAT) / SUM(reps) AS weightPerRep " +
                "FROM 'Set' " +
                "WHERE exercise_id == :exerciseId " +
                "AND ignore_in_stat==0 " +
                "AND date >= :start AND date <= :end " +
                "GROUP BY strftime ('%F', date, 'unixepoch')"
    )
    fun getWeightPerRepHistory(exerciseId: Long, start: Long, end: Long): List<WeightOnDate>
}