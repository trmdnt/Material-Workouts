package eu.trmdnt.workouts.database

import androidx.room3.*
import androidx.room3.Dao
import eu.trmdnt.workouts.database.entities.*
import eu.trmdnt.workouts.database.entities.statistics.WorkoutWithDuration
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    //Workouts
    @Query(
        """ 
WITH timePerWorkout AS (
    SELECT
        w.id AS id,
        MAX(s.date) - MIN(s.date) AS timeSpentSeconds
    FROM Workout w
    LEFT JOIN Exercise e ON e.workout_id = w.id
    LEFT JOIN `Set` s ON s.exercise_id = e.exercise_id
    GROUP BY w.id
) 
SELECT * FROM workout
NATURAL JOIN timePerWorkout
ORDER BY dateStarted DESC
        """
    )
    fun getWorkoutsWithDuration(): Flow<List<WorkoutWithDuration>>

    //Templates
    @Query(
        "SELECT * FROM workout_template"
    )
    fun getWorkoutTemplates(): Flow<List<WorkoutTemplate>>

    @Query(
        """
SELECT workout_template.*, max(Workout.dateStarted) as lastUsed FROM workout_template
LEFT JOIN Workout 
	ON Workout.workout_template_id = workout_template.workout_template_id
GROUP BY workout_template.workout_template_id
        """
    )
    fun getWorkoutTemplatesWithLastUsed(): Flow<List<WorkoutTemplateWithLastUsed>>

    @Query(
        "SELECT * FROM workout_template where workout_template_id = :workoutTemplateId"
    )
    fun getWorkoutTemplateById(workoutTemplateId: Long): Flow<WorkoutTemplate>

    @Insert
    fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long

    @Query("delete from workout_template where workout_template_id = :id")
    fun deleteWorkoutTemplateWithId(id: Long)

    @Delete
    fun deleteWorkoutTemplate(vararg workoutTemplates: WorkoutTemplate)

    @Update
    fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate)


    @Query("SELECT * FROM exercise_template")
    fun getAllExerciseTemplates(): Flow<List<ExerciseTemplate>>

    @Query("SELECT * FROM exercise_template WHERE name LIKE '%' || :query || '%'")
    fun searchExerciseTemplates(query: String): Flow<List<ExerciseTemplate>>

    @Query("SELECT * FROM exercise_template WHERE exercise_template_id = :exerciseTemplateId")
    fun getExerciseTemplateById(exerciseTemplateId: Long): Flow<ExerciseTemplate>

    @Query("SELECT exercise_template.* FROM exercise_template JOIN workout_exercise_template_cross_ref ON workout_exercise_template_cross_ref.exercise_template_id=exercise_template.exercise_template_id WHERE workout_template_id = :workoutTemplateId")
    fun getAllExercisesFromWorkoutTemplate(workoutTemplateId: Long): Flow<List<ExerciseTemplate>>


    @Insert
    fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplate): Long

    @Update
    fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate)

    @Delete
    fun deleteExerciseTemplates(vararg exerciseTemplate: ExerciseTemplate)

    @Insert
    fun insertWorkoutExerciseTemplateCrossRef(exerciseTemplateCrossRef: WorkoutExerciseTemplateCrossRef)

    @Delete
    fun deleteWorkoutTemplateCrossRef(workoutExerciseTemplateCrossRef: WorkoutExerciseTemplateCrossRef): Int

    @Insert
    fun insertWorkout(workout: Workout): Long

    @Delete
    fun deleteWorkouts(vararg workouts: Workout)

    @Query("SELECT * FROM workout WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Long): Flow<Workout>

    @Insert
    fun insertExercise(exercise: Exercise)

    @Transaction
    @Query("SELECT * FROM exercise WHERE workout_id = :workoutId")
    fun getExercisesByWorkoutId(workoutId: Long): Flow<List<ExerciseWithSets>>

    @Transaction
    @Query("SELECT * FROM exercise WHERE exercise_id = :exerciseId")
    fun getExerciseWithSetsById(exerciseId: Long): ExerciseWithSets?

    @Query("DELETE FROM exercise WHERE exercise_id = :exerciseId")
    fun deleteExerciseById(exerciseId: Long)

    @Insert
    fun insertSets(vararg exerciseSet: ExerciseSet): List<Long>

    @Update
    fun updateSet(exerciseSet: ExerciseSet)

    @Query("DELETE FROM `set` WHERE id = :setId")
    fun deleteSetById(setId: Long)

    @Query("SELECT * FROM exercise WHERE exercise_id = :id")
    fun getExerciseById(id: Long): Exercise?

    @Query("SELECT * FROM `set` WHERE id = :id")
    fun getSetById(id: Long): ExerciseSet?

    @Update
    fun updateWorkout(workout: Workout)
}