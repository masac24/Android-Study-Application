package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diplomski.database.tables.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder WHERE userId = :userId ORDER BY id DESC")
    fun getAllReminders(userId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminder WHERE id = :id")
    fun getReminder(id: Int): Flow<Reminder>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder) : Long

    @Query("DELETE FROM reminder WHERE userId = :id")
    suspend fun deleteByUserId(id: Int)

    @Query("DELETE FROM reminder WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    @Query("DELETE FROM reminder")
    suspend fun deleteAll()

}