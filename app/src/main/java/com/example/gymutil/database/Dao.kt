package com.example.gymutil.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.example.gymutil.database.entities.*


@Dao
interface Dao {

    //Workouts
    @Query(
        "SELECT * FROM workout"
    )
    fun getWorkouts(): LiveData<Workout>

    @Query(
        "SELECT * FROM workout JOIN exercise ON workout.id = exercise.workout_id JOIN `Set` ON exercise.exercise_id = `Set`.exercise_id"
    )
    fun getWorkoutsAndExercisesAnd(): LiveData<Map<Workout, Map<Exercise, ExerciseSet>>>

    //Templates
    @Query(
        "SELECT * FROM workout_template"
    )
    fun getWorkoutTemplates(): LiveData<List<WorkoutTemplate>>

    @Query(
        "SELECT * FROM workout_template where workout_template_id = :workoutTemplateId"
    )
    fun getWorkoutTemplateByIdLive(workoutTemplateId: Long): LiveData<WorkoutTemplate>

    @Query(
        "SELECT * FROM workout_template where workout_template_id = :workoutTemplateId"
    )
    fun getWorkoutTemplateById(workoutTemplateId: Long): WorkoutTemplate

    @Insert
    fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long

    // is a delete query
    @Query("delete from workout_template where workout_template_id = :id")
    fun deleteWorkoutTemplateWithId(id: Long)

    @Delete
    fun deleteWorkoutTemplate(vararg workoutTemplates: WorkoutTemplate)

    @Update
    fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate)


    @Transaction
    @Query("SELECT * FROM workout_template")
    fun getWorkoutTemplatesWithExercises(): LiveData<List<WorkoutTemplateWithExercises>>

    @Query("SELECT * FROM exercise_template")
    fun getAllExerciseTemplates(): LiveData<List<ExerciseTemplate>>

    @Query("SELECT * FROM exercise_template WHERE exercise_template_id = :exerciseTemplateId")
    fun getExerciseTemplateById(exerciseTemplateId: Long): LiveData<ExerciseTemplate>

//    @Query("SELECT * FROM exercise_template WHERE exercise_template_id = :exerciseTemplateId")
//    fun getExerciseTemplateByIdInstant(exerciseTemplateId: Long): ExerciseTemplate


    @Query("SELECT exercise_template.* FROM exercise_template JOIN workout_exercise_template_cross_ref ON workout_exercise_template_cross_ref.exercise_template_id=exercise_template.exercise_template_id WHERE workout_template_id = :workoutTemplateId")
    fun getAllExercisesFromWorkoutTemplate(workoutTemplateId: Long): LiveData<List<ExerciseTemplate>>

//    @Insert
//    fun insertAllExerciseTemplates(vararg exerciseTemplates: ExerciseTemplate)

    @Insert
    fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplate): Long

    @Update
    fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate)

    @Delete
    fun deleteExerciseTemplates(vararg exerciseTemplate: ExerciseTemplate)

    @Transaction
    @Query("SELECT * FROM workout_template")
    fun getAllWorkoutTemplatesWithExercises(): LiveData<List<WorkoutTemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_template WHERE workout_template_id = :id")
    fun getWorkoutTemplateWithExercisesById(id: Long): LiveData<WorkoutTemplateWithExercises>

    @Insert
    fun insertWorkoutExerciseTemplateCrossRef(exerciseTemplateCrossRef: WorkoutExerciseTemplateCrossRef)

    @Delete
    fun deleteWorkoutTemplateCrossRef(workoutExerciseTemplateCrossRef: WorkoutExerciseTemplateCrossRef): Int

    @Query("SELECT * FROM workout ORDER BY id DESC")
    fun getAllWorkouts(): LiveData<List<Workout>>

    @Insert
    fun insertWorkout(workout: Workout): Long

    @Delete
    fun deleteWorkouts(vararg workouts: Workout)

    @Query("SELECT * FROM workout WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Long): LiveData<Workout>

    @Insert
    fun insertExercise(exercise: Exercise)

    @Transaction
    @Query("SELECT * FROM exercise WHERE workout_id = :workoutId")
    fun getExercisesByWorkoutId(workoutId: Long): LiveData<List<ExerciseWithSets>>

    @Query("DELETE FROM exercise WHERE exercise_id = :exerciseId")
    fun deleteExerciseById(exerciseId: Long)

    @Insert
    fun insertSet(exerciseSet: ExerciseSet)

    @Update
    fun updateSet(exerciseSet: ExerciseSet)

    @Query("DELETE FROM `set` WHERE id = :setId")
    fun deleteSetById(setId: Long)


}