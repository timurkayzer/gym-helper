package com.gymhelper.app.interval

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymhelper.app.GymHelperApp
import com.gymhelper.app.util.GongPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IntervalSessionViewModel(
    application: Application,
    private val dayId: Long,
) : AndroidViewModel(application) {

    private val repository = (application as GymHelperApp).repository
    private val gongPlayer = GongPlayer(application)

    private val _state = MutableStateFlow(IntervalSessionState())
    val state: StateFlow<IntervalSessionState> = _state.asStateFlow()

    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            val day = repository.getIntervalDay(dayId)
            val exercises = repository.getIntervalExercises(dayId)
            if (day == null) {
                _state.value = IntervalSessionState(
                    phases = listOf(IntervalPhase(IntervalPhaseKind.FINISHED, "Day not found", 0)),
                )
                return@launch
            }
            val phases = buildIntervalPhases(
                exercises = exercises.map { exercise ->
                    IntervalExercisePhaseInput(
                        name = exercise.name,
                        durationSeconds = exercise.durationSeconds ?: day.roundSeconds,
                    )
                },
                restBetweenExercisesSeconds = day.restBetweenExercisesSeconds,
                restBetweenRoundsSeconds = day.restBetweenRoundsSeconds,
                rounds = day.rounds,
            )
            val first = phases.first()
            _state.value = IntervalSessionState(
                phases = phases,
                currentPhaseIndex = 0,
                secondsRemaining = first.durationSeconds,
            )
        }
    }

    fun start() {
        if (_state.value.isRunning || _state.value.isFinished) return
        _state.update { it.copy(isRunning = true, isPaused = false, sessionStarted = true) }
        startTicker()
    }

    fun pause() {
        _state.update { it.copy(isPaused = true) }
        tickerJob?.cancel()
    }

    fun resume() {
        if (_state.value.isFinished) return
        _state.update { it.copy(isPaused = false, isRunning = true) }
        startTicker()
    }

    fun stop() {
        tickerJob?.cancel()
        _state.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                currentPhaseIndex = it.phases.lastIndex,
                secondsRemaining = 0,
            )
        }
    }

    fun abandon() {
        tickerJob?.cancel()
        _state.update { it.copy(isRunning = false, isPaused = false) }
    }

    fun skipToNextPhase() {
        if (!_state.value.canSkipPhase) return
        tickerJob?.cancel()
        advancePhase()
        val snapshot = _state.value
        if (snapshot.isFinished) {
            _state.update { it.copy(isRunning = false, isPaused = false) }
            return
        }
        _state.update { it.copy(isPaused = false, isRunning = true) }
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val snapshot = _state.value
                if (snapshot.isPaused || !snapshot.isRunning || snapshot.isFinished) break
                delay(1000)
                val current = _state.value
                if (current.isPaused || !current.isRunning) break
                if (current.secondsRemaining > 1) {
                    _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
                } else {
                    advancePhase()
                }
            }
        }
    }

    private fun advancePhase() {
        val current = _state.value
        val phase = current.currentPhase
        if (phase != null && phase.kind != IntervalPhaseKind.FINISHED) {
            gongPlayer.playGong()
        }
        val nextIndex = current.currentPhaseIndex + 1
        val nextPhase = current.phases.getOrNull(nextIndex)
        if (nextPhase == null) {
            _state.update { it.copy(isRunning = false, secondsRemaining = 0) }
            return
        }
        _state.update {
            it.copy(
                currentPhaseIndex = nextIndex,
                secondsRemaining = nextPhase.durationSeconds,
                isRunning = nextPhase.kind != IntervalPhaseKind.FINISHED,
            )
        }
        if (nextPhase.kind == IntervalPhaseKind.FINISHED) {
            gongPlayer.playGong()
            tickerJob?.cancel()
        }
    }

    override fun onCleared() {
        gongPlayer.release()
        super.onCleared()
    }
}

class IntervalSessionViewModelFactory(
    private val application: Application,
    private val dayId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IntervalSessionViewModel::class.java)) {
            return IntervalSessionViewModel(application, dayId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
