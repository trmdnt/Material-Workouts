package eu.trmdnt.workouts.database

import androidx.room.Dao
import androidx.room.Query
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate

@Dao
interface StatisticsDao {
    @Query(
        """
        SELECT
            substr(datetime(CAST(s.date AS TEXT), 'unixepoch'), 1, 10) AS date,
            SUM(s.reps * s.weight) AS totalWeight,
            SUM(s.reps) AS totalReps,
            CAST(SUM(s.reps * s.weight) AS FLOAT) / SUM(s.reps) AS weightPerRep,
            MIN(s.date) AS dateTimeStamp
        FROM `Set` s
        INNER JOIN `Exercise` e ON s.exercise_id = e.exercise_id
        WHERE
            e.exercise_template_id = :exerciseTemplateId AND
            s.ignore_in_stat = 0 AND
            s.date >= :start AND
            s.date <= :end
        GROUP BY
            substr(datetime(CAST(s.date AS TEXT), 'unixepoch'), 1, 10)
    """
    )
    fun getWeightPerRepHistory(exerciseTemplateId: Long, start: Long, end: Long): List<WeightOnDate>

    @Query(
        """
            SELECT *
            FROM `Set` s
            INNER JOIN exercise e ON s.exercise_id = e.exercise_id
            WHERE substr(datetime(CAST(s.date AS TEXT), 'unixepoch'), 1, 10) == substr(datetime(CAST(:dayTimeStamp AS TEXT), 'unixepoch'), 1, 10) AND
                e.exercise_template_id == :exerciseTemplateId
        """
    )
    fun getExerciseHistoryOnDay(exerciseTemplateId: Long, dayTimeStamp: Long): List<ExerciseSet>
}