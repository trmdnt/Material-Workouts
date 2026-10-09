package eu.trmdnt.workouts.database

import eu.trmdnt.workouts.database.entities.*
import eu.trmdnt.workouts.database.entities.statistics.WorkoutWithDuration
import kotlinx.coroutines.flow.Flow

class GymRepository(private val dao: Dao) {
    //WorkoutTemplates
    fun getAllWorkoutTemplates(): Flow<List<WorkoutTemplate>> = dao.getWorkoutTemplates()

    fun getAllWorkoutTemplatesWithLastUsed(): Flow<List<WorkoutTemplateWithLastUsed>> = dao.getWorkoutTemplatesWithLastUsed()

    fun getWorkoutTemplateById(workoutTemplateId: Long): Flow<WorkoutTemplate> =
        dao.getWorkoutTemplateById(workoutTemplateId)

    fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long {
        return dao.insertWorkoutTemplate(workoutTemplate)
    }

    fun deleteWorkoutTemplates(workoutTemplates: List<WorkoutTemplate>) {
        dao.deleteWorkoutTemplate(*workoutTemplates.toTypedArray())
    }

    fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate) {
        dao.updateWorkoutTemplate(workoutTemplate)
    }

    //ExerciseTemplates
    fun getAllExerciseTemplatesFromWorkoutTemplate(workoutTemplateId: Long): Flow<List<ExerciseTemplate>> =
        dao.getAllExercisesFromWorkoutTemplate(workoutTemplateId)

    fun getAllExerciseTemplates(): Flow<List<ExerciseTemplate>> = dao.getAllExerciseTemplates()

    fun searchExerciseTemplates(query: String): Flow<List<ExerciseTemplate>> =
        dao.searchExerciseTemplates(query)

    fun getExerciseTemplateById(id: Long) = dao.getExerciseTemplateById(id)

    fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplate) =
        dao.insertExerciseTemplate(exerciseTemplate)

    fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate) =
        dao.updateExerciseTemplate(exerciseTemplate)

    fun deleteExerciseTemplates(exerciseTemplates: List<ExerciseTemplate>) =
        dao.deleteExerciseTemplates(*exerciseTemplates.toTypedArray())

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


    fun getAllWorkoutsWithDuration(): Flow<List<WorkoutWithDuration>> =
        dao.getWorkoutsWithDuration()

    fun createWorkout(workout: Workout): Long {
        return dao.insertWorkout(workout)
    }

    fun deleteWorkouts(workouts: List<Workout>) {
        dao.deleteWorkouts(*workouts.toTypedArray())

    }

    fun getWorkoutById(id: Long): Flow<Workout> {
        return dao.getWorkoutById(id)
    }

    fun updateWorkout(workout: Workout) {
        dao.updateWorkout(workout)
    }


    //Exercises
    fun insertExercise(exercise: Exercise) {
        return dao.insertExercise(exercise)
    }

    fun deleteExerciseById(exerciseId: Long) {
        dao.deleteExerciseById(exerciseId)
    }

    fun getAllExercisesByWorkoutId(workoutId: Long): Flow<List<ExerciseWithSets>> {
        return dao.getExercisesByWorkoutId(workoutId)
    }

    fun getExerciseWithSets(id: Long): ExerciseWithSets? {
        return dao.getExerciseWithSetsById(id)
    }


    //Sets
    fun insertSet(set: ExerciseSet): List<Long> {
        return dao.insertSets(set)
    }

    fun insertSets(sets: List<ExerciseSet>) {
        dao.insertSets(*sets.toTypedArray())
    }

    fun updateSet(set: ExerciseSet) {
        dao.updateSet(set)
    }

    fun deleteSetById(setId: Long) {
        dao.deleteSetById(setId)
    }

    fun getSetById(id: Long): ExerciseSet? {
        return dao.getSetById(id)
    }
}