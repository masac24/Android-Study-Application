package com.example.diplomski.viewmodels

import android.hardware.SensorEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Session
import com.example.diplomski.repositories.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.collections.reversed
import kotlin.math.abs

data class uiStateSession(
    val maxStreak : Int = 0,
    val streak : Int = 0,
    val doneToday: Int = 0,
    val duration: Int = 0,
    val progress: Int = 0,
    val isRunning: Boolean = false,
    val hasBeenWarned: Boolean = false,
    val showWarning: Boolean = false,
    val canceled: Boolean = false,
    val completed: Boolean = false,
    val id: Int = 0
)

class SessionViewModel( private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions = _sessions.asStateFlow()

    var uiStateSession by mutableStateOf(uiStateSession())
        private set

    fun maxStreak(){
        var maxStreak = 1
        var streak = 1

        val sess = sessions.value.filter { it.completed }.sortedBy { it.datetime }
        if (sess.isEmpty()) {
            uiStateSession = uiStateSession.copy(maxStreak = 0, streak = 0, doneToday = 0)
            return
        }
        val doneToday = sess.count { session ->
            checkDates(session.datetime, System.currentTimeMillis(), true)
        }
        var yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val doneYesterday = sess.count {session ->
            checkDates(session.datetime, yesterday.timeInMillis, true)
        }

        for (i in 0 until sess.size - 1) {
            when {
                checkDates(sess[i].datetime, sess[i + 1].datetime, true) -> { }
                checkDates(sess[i].datetime, sess[i + 1].datetime, false) -> {
                    streak++
                    if (streak > maxStreak) maxStreak = streak
                }
                else -> {
                    streak = 1
                }
            }
        }

        uiStateSession = uiStateSession.copy(maxStreak = maxStreak, streak = streak, doneToday = doneToday)
        if(doneToday == 0 && doneYesterday == 0) uiStateSession = uiStateSession.copy(streak = 0)
    }


    fun getSessions(id: Int){
        uiStateSession = uiStateSession.copy(id = id)
        viewModelScope.launch {
            sessionRepository.getAllSessions(id).collect { s ->
                _sessions.value = s
                maxStreak()
            }
        }
    }

    fun setDuration(i: Int){
        uiStateSession = uiStateSession.copy(duration = i)
    }

    private var sessionJob: Job? = null
    fun startSession(id: Int){
        val datetime = System.currentTimeMillis()
        uiStateSession = uiStateSession.copy(isRunning = true, canceled = false, completed = false)
        sessionJob = viewModelScope.launch {
            val totalSeconds = uiStateSession.duration * 60 + 1
            for (i in 0 until totalSeconds) {
                if (!uiStateSession.isRunning) {
                    while (!uiStateSession.isRunning) {
                        delay(100L)
                    }
                }
                uiStateSession = uiStateSession.copy(progress = i)
                delay(1000L)
            }
            sessionRepository.insert(Session(0, id, uiStateSession.duration, datetime, true))
            uiStateSession = uiStateSession.copy(isRunning = false, completed = true)
        }
    }
    private var lastMovementTime = 0L
    fun onMovementDetected() {
        val now = System.currentTimeMillis()
        if (now - lastMovementTime < 2000L) return
        lastMovementTime = now
        if (!uiStateSession.isRunning) return
        if (uiStateSession.hasBeenWarned) {
            cancelSession()
        } else {
            uiStateSession = uiStateSession.copy(isRunning = false, showWarning = true)
        }
    }

    fun continueSession() {
        uiStateSession = uiStateSession.copy(isRunning = true, showWarning = false, hasBeenWarned = true)
    }

    fun cancelSession() {
        sessionJob?.cancel()
        uiStateSession = uiStateSession.copy(isRunning = false,showWarning = false,
            progress = 0, hasBeenWarned = false, canceled = true)
        viewModelScope.launch {
            sessionRepository.insert(Session(0, uiStateSession.id,
                uiStateSession.duration, System.currentTimeMillis(), false))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val sessionRepository = (this[APPLICATION_KEY] as StudyApplication).sessionRepository
                return@initializer SessionViewModel(sessionRepository)
            }
        }
    }

    fun checkDates(millis1: Long, millis2: Long, same: Boolean): Boolean{
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }

        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        cal1.set(Calendar.MILLISECOND, 0)

        cal2.set(Calendar.HOUR_OF_DAY, 0)
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)
        cal2.set(Calendar.MILLISECOND, 0)

        val diff = abs(cal1.timeInMillis - cal2.timeInMillis)
        return if (same) 0L == diff else diff == 24 * 60 * 60 * 1000L
    }

    fun avgSessionsPerDay(): Float {
        val completed = sessions.value.filter { it.completed }
        if (completed.isEmpty()) return 0f

        val uniqueDays = completed.map { session ->
            Calendar.getInstance().apply {
                timeInMillis = session.datetime
            }.let {
                "${it.get(Calendar.YEAR)}-${it.get(Calendar.DAY_OF_YEAR)}"
            }
        }.toSet().size

        return completed.size.toFloat() / uniqueDays.toFloat()
    }

    fun totalTime() : Int{
        val completed = sessions.value.filter { it.completed }
        if (completed.isEmpty()) return 0
        return completed.sumOf { it.time }
    }

    fun totalTimePerDay() : List<Int>{
        val totals = mutableStateListOf<Int>()
        val completed = sessions.value.filter { it.completed }
        if (completed.isEmpty()) return listOf(0,0,0,0,0,0,0)
        val date = Calendar.getInstance()
        for (i in 0 until 7){
            val day = sessions.value.filter { checkDates(it.datetime, date.timeInMillis, true) }
            totals.add(day.sumOf { it.time })
            date.add(Calendar.DAY_OF_YEAR, -1)
        }
        return totals.reversed()
    }

}