package com.gymhelper.app.weight

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymhelper.app.GymHelperApp
import com.gymhelper.app.data.WeightExercise
import com.gymhelper.app.data.WeightSessionSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeightSessionUiState(
    val dayId: Long = 0,
    val dayName: String = "",
    val exercises: List<WeightExercise> = emptyList(),
    val exerciseIndex: Int = 0,
    val setNumber: Int = 1,
    val weightInput: String = "",
    val loggedSets: List<WeightSessionSet> = emptyList(),
    val finished: Boolean = false,
    val saving: Boolean = false,
) {
    val currentExercise: WeightExercise? = exercises.getOrNull(exerciseIndex)
}

class WeightSessionViewModel(
    application: Application,
    private val dayId: Long,
) : AndroidViewModel(application) {

    private val repository = (application as GymHelperApp).repository

    private val _state = MutableStateFlow(WeightSessionUiState(dayId = dayId))
    val state: StateFlow<WeightSessionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val day = repository.getWeightDay(dayId)
            val exercises = repository.getWeightExercises(dayId)
            val first = exercises.firstOrNull()
            _state.value = WeightSessionUiState(
                dayId = dayId,
                dayName = day?.name ?: "",
                exercises = exercises,
                weightInput = formatWeight(first?.lastWeightKg),
            )
        }
    }

    fun onWeightChange(value: String) {
        _state.update { it.copy(weightInput = value) }
    }

    fun completeSet() {
        val snapshot = _state.value
        val exercise = snapshot.currentExercise ?: return
        val weight = snapshot.weightInput.replace(',', '.').toDoubleOrNull()
        if (weight == null || weight < 0) return

        val newSet = WeightSessionSet(
            sessionId = 0,
            exerciseId = exercise.id,
            setNumber = snapshot.setNumber,
            weightKg = weight,
        )
        val updatedSets = snapshot.loggedSets + newSet

        if (snapshot.setNumber < exercise.sets) {
            _state.update {
                it.copy(
                    setNumber = it.setNumber + 1,
                    loggedSets = updatedSets,
                )
            }
            return
        }

        val nextIndex = snapshot.exerciseIndex + 1
        if (nextIndex >= snapshot.exercises.size) {
            _state.update {
                it.copy(loggedSets = updatedSets, finished = true)
            }
            persistSession(updatedSets)
            return
        }

        val nextExercise = snapshot.exercises[nextIndex]
        _state.update {
            it.copy(
                exerciseIndex = nextIndex,
                setNumber = 1,
                loggedSets = updatedSets,
                weightInput = formatWeight(nextExercise.lastWeightKg),
            )
        }
    }

    private fun persistSession(sets: List<WeightSessionSet>) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val lastWeights = sets.groupBy { it.exerciseId }
                .mapValues { (_, exerciseSets) -> exerciseSets.maxBy { it.setNumber }.weightKg }
            repository.saveWeightSession(
                dayId = dayId,
                completedAtEpochMs = System.currentTimeMillis(),
                sets = sets,
                exerciseLastWeights = lastWeights,
            )
            _state.update { it.copy(saving = false) }
        }
    }

    private fun formatWeight(weight: Double?): String {
        if (weight == null) return ""
        return if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()
    }
}

class WeightSessionViewModelFactory(
    private val application: Application,
    private val dayId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightSessionViewModel::class.java)) {
            return WeightSessionViewModel(application, dayId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
