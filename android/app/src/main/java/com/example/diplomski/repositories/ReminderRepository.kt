package com.example.diplomski.repositories


import com.example.diplomski.database.daos.ReminderDao
import com.example.diplomski.database.tables.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(id: Int) : Flow<List<Reminder>> {
        return reminderDao.getAllReminders(id)
    }

    suspend fun insert(reminder: Reminder) : Long {
        return reminderDao.insert(reminder)
    }

    suspend fun delete(id: Int){
        return reminderDao.deleteReminder(id)
    }
}