package com.example.diplomski.viewmodels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.ReminderReceiver
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Reminder
import com.example.diplomski.repositories.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class uiReminderState(
    val date : Long? = null,
    val time : Long? = null,
    val name : String = ""
)


class ReminderViewModel(application: Application, private val reminderRepository: ReminderRepository
) : AndroidViewModel(application) {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders = _reminders.asStateFlow()

    var uiReminderState by mutableStateOf(uiReminderState())
        private set

    fun setDate(date: Long?){
        uiReminderState = uiReminderState.copy(date = date)
    }

    fun setTime(time: Long?){
        uiReminderState = uiReminderState.copy(time = time)
    }

    fun setName(name: String){
        uiReminderState = uiReminderState.copy(name = name)
    }

    fun combineDateAndTime(dateMillis: Long, timeMillis: Long): Long {
        val dateCalendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val timeCalendar = Calendar.getInstance().apply { timeInMillis = timeMillis }

        return Calendar.getInstance().apply {
            set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }


    fun scheduleReminder(reminder: Reminder) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.text)
            putExtra("id", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.time,
            pendingIntent
        )
    }

    fun deleteReminder(reminder: Reminder) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.text)
            putExtra("id", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        viewModelScope.launch {
            reminderRepository.delete(reminder.id)
        }
    }

    private val appContext = getApplication<Application>()

    fun insertReminder(id: Int){
        viewModelScope.launch {
            if(uiReminderState.date != null && uiReminderState.time != null){
                val sum = combineDateAndTime(uiReminderState.date!!, uiReminderState.time!!)

                val reminder = Reminder(0, uiReminderState.name, id, sum)
                val idr = reminderRepository.insert(reminder)
                scheduleReminder(Reminder(idr.toInt(), uiReminderState.name, id, sum))
            }
        }
    }

    fun getReminders(id: Int) {
        viewModelScope.launch {
            reminderRepository.getAllReminders(id).collect {r ->
                _reminders.value = r
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val reminderRepository = (this[APPLICATION_KEY] as StudyApplication).reminderRepository
                return@initializer ReminderViewModel(application, reminderRepository)
            }
        }
    }
}