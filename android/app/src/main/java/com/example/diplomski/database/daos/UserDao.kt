package com.example.diplomski.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.diplomski.database.tables.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: Int): Flow<User>

    @Query("SELECT * FROM users WHERE username = :username AND password = :password ")
    fun findUser(username: String, password: String): Flow<User?>

    @Query("SELECT * FROM users WHERE username = :username ")
    fun checkIfExist(username: String): Flow<User?>

    @Query("SELECT * FROM users WHERE logged = 1 ")
    fun getLogged(): Flow<User?>

    @Update
    suspend fun update(user : User)
    @Update
    suspend fun changeLogged(user: User)

    @Query("SELECT * FROM users WHERE syncId IS NULL")
    suspend fun getUnsynced(): List<User>

    @Query("UPDATE users SET syncId = :syncId WHERE id = :id")
    suspend fun updateSyncId(id: Int, syncId: Long)

    @Query("SELECT * FROM users WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: Long): User?


}
