package com.example.gymutil.database

import androidx.lifecycle.LiveData
import com.example.gymutil.database.entities.*

class GymRepository(private val dao: Dao) {
    fun getAllWorkoutTemplates(): LiveData<List<WorkoutTemplate>> = dao.getWorkoutTemplates()

    fun getWorkoutTemplateByIdLive(workoutTemplateId: Long): LiveData<WorkoutTemplate> =
        dao.getWorkoutTemplateByIdLive(workoutTemplateId)

    fun getWorkoutTemplateById(workoutTemplateId: Long): WorkoutTemplate =
        dao.getWorkoutTemplateById(workoutTemplateId)

    fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long {
        return dao.insertWorkoutTemplate(workoutTemplate)
    }

    fun deleteWorkoutTemplates(workoutTemplates: List<WorkoutTemplate>) {
        dao.deleteWorkoutTemplate(*workoutTemplates.toTypedArray())
    }

    fun getAllExerciseTemplatesFromWorkoutTemplate(workoutTemplateId: Long): LiveData<List<ExerciseTemplate>> =
        dao.getAllExercisesFromWorkoutTemplate(workoutTemplateId)

    fun getAllExerciseTemplates(): LiveData<List<ExerciseTemplate>> = dao.getAllExerciseTemplates()

    //    fun insertAllExerciseTemplates(vararg exerciseTemplate: ExerciseTemplate) {
//        dao.insertAllExerciseTemplates(*exerciseTemplate)
//    }
    fun getExerciseTemplateById(id: Long): LiveData<ExerciseTemplate> {
        println("trying to get $id")
        val exerciseTemplate = dao.getExerciseTemplateById(id)

        println(exerciseTemplate.value)
        return exerciseTemplate
    }

//    fun getExerciseTemplateByIdNow(id: Long): ExerciseTemplate {
//        println("trying to get $id")
//        val exerciseTemplate = dao.getExerciseTemplateByIdInstant(id)
//        sleep(500)
//        println(exerciseTemplate)
//        return exerciseTemplate
//    }


    fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplate): Long {
        return dao.insertExerciseTemplate(exerciseTemplate)
    }

    fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate) {
        dao.updateExerciseTemplate(exerciseTemplate)
    }

    fun deleteExerciseTemplates(exerciseTemplates: List<ExerciseTemplate>) {
        dao.deleteExerciseTemplates(*exerciseTemplates.toTypedArray())
    }

    fun addExerciseToWorkoutTemplate(workoutTemplateId: Long, exerciseTemplateId: Long) {
        dao.insertWorkoutExerciseTemplateCrossRef(
            WorkoutExerciseTemplateCrossRef(
                workoutTemplateId, exerciseTemplateId
            )
        )
    }

    fun removeExerciseFromWorkoutTemplate(workoutTemplateId: Long, exerciseTemplateId: Long) {
        dao.deleteWorkoutTemplateCrossRef(
            WorkoutExerciseTemplateCrossRef(
                workoutTemplateId = workoutTemplateId, exerciseTemplateId = exerciseTemplateId
            )
        )
    }

    fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate) {
        dao.updateWorkoutTemplate(workoutTemplate)
    }

    fun getAllWorkouts(): LiveData<List<Workout>> {
        return dao.getAllWorkouts();
    }

    fun createWorkout(workout: Workout): Long {

        return dao.insertWorkout(workout)
    }

    fun deleteWorkouts(workouts: List<Workout>) {
        dao.deleteWorkouts(*workouts.toTypedArray())

    }

    fun getWorkoutById(id: Long): LiveData<Workout> {
        return dao.getWorkoutById(id)
    }

    fun insertExercise(exercise: Exercise) {
        return dao.insertExercise(exercise)
    }

    fun getAllExercisesByWorkoutId(workoutId: Long): LiveData<List<ExerciseWithSets>> {
        return dao.getExercisesByWorkoutId(workoutId)
    }

    fun insertSet(set: ExerciseSet) {
        dao.insertSet(set)
    }

    fun updateSet(set: ExerciseSet) {
        dao.updateSet(set)
    }

    fun deleteExerciseById(exerciseId: Long) {
        dao.deleteExerciseById(exerciseId)
    }

    fun deleteSetById(setId: Long) {
        dao.deleteSetById(setId)
    }
}