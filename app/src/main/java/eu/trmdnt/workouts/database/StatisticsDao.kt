package eu.trmdnt.workouts.database

import androidx.room3.Dao
import androidx.room3.Query
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.statistics.LastWeightForExercise
import eu.trmdnt.workouts.database.entities.statistics.RecentStatistics
import eu.trmdnt.workouts.database.entities.statistics.WeightOnDate
import kotlinx.coroutines.flow.Flow

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
            s.set_type = 'Default' AND
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

    @Query(
        """
            WITH timePerWorkout AS (
  SELECT
    w.id AS workoutId,
    MAX(s.date) - MIN(s.date) AS timeSpentSeconds,
    w.dateStarted AS dateStartedEpoch
  FROM Workout w
  JOIN Exercise e ON e.workout_id = w.id
  JOIN `Set` s ON s.exercise_id = e.exercise_id
  GROUP BY w.id
)
SELECT
  COALESCE(SUM(CASE WHEN dateStartedEpoch >= strftime('%s','now','-7 days')   THEN timeSpentSeconds ELSE 0 END), 0) AS timeWorkingOutWeek,
  COALESCE(SUM(CASE WHEN dateStartedEpoch >= strftime('%s','now','-30 days')  THEN timeSpentSeconds ELSE 0 END), 0) AS timeWorkingOutMonth,
  COALESCE(SUM(CASE WHEN dateStartedEpoch >= strftime('%s','now','-365 days') THEN timeSpentSeconds ELSE 0 END), 0) AS timeWorkingOutYear,
  COALESCE(SUM(timeSpentSeconds), 0) as timeWorkingOutTotal,

  COALESCE(SUM(CASE WHEN dateStartedEpoch >= strftime('%s','now','-7 days')   THEN 1 ELSE 0 END), 0) AS workoutCountWeek,
  COALESCE(SUM(CASE WHEN dateStartedEpoch >= strftime('%s','now','-30 days')  THEN 1 ELSE 0 END), 0) AS workoutCountMonth,
  COALESCE(SUM(CASE WHEN dateStartedEpoch >= strftime('%s','now','-365 days') THEN 1 ELSE 0 END), 0) AS workoutCountYear,
  COALESCE(count(*), 0) AS workoutCountTotal
FROM timePerWorkout;
        """
    )
    fun getRecentActivityStats(): Flow<RecentStatistics>

    @Query(
        """
WITH last_weight as (
	SELECT set_type, max(date), weight FROM `Set`
	JOIN Exercise ON
		Exercise.exercise_id == `Set`.exercise_id
	WHERE exercise_template_id = :exerciseTemplateId
	GROUP BY set_type
) 
SELECT
	total(weight) filter (where set_type LIKE 'default') as "default",
	total(weight) filter (where set_type LIKE 'warmup') as "warmup",
	total(weight) filter (where set_type LIKE 'drop') as "drop"
FROM last_weight
        """
    )
    fun getLastWeightByExerciseTemplate(exerciseTemplateId: Long): LastWeightForExercise
}