package eu.trmdnt.workouts.database

import androidx.lifecycle.LiveData
import eu.trmdnt.workouts.database.entities.Exercise
import eu.trmdnt.workouts.database.entities.ExerciseSet
import eu.trmdnt.workouts.database.entities.ExerciseTemplate
import eu.trmdnt.workouts.database.entities.ExerciseWithSets
import eu.trmdnt.workouts.database.entities.Workout
import eu.trmdnt.workouts.database.entities.WorkoutExerciseTemplateCrossRef
import eu.trmdnt.workouts.database.entities.WorkoutTemplate

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
        dao.insertSets(set)
    }

    fun insertSets(sets: List<ExerciseSet>) {
        dao.insertSets(*sets.toTypedArray())
    }


    fun updateSet(set: ExerciseSet) {
        dao.updateSet(set)
    }

    fun deleteExerciseById(exerciseId: Long) {
        dao.deleteExerciseById(exerciseId)
    }

    fun getExerciseById(id: Long): Exercise? {
        return dao.getExerciseById(id)
    }

    fun deleteSetById(setId: Long) {
        dao.deleteSetById(setId)
    }

    fun getSetById(id: Long): ExerciseSet? {
        return dao.getSetById(id)
    }

    fun getExerciseWithSets(id: Long): ExerciseWithSets? {
        return dao.getExerciseWithSetsById(id)
    }

    fun updateWorkout(workout: Workout) {
        dao.updateWorkout(workout)
    }
}