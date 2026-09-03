package com.example.diplomski.database.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "sender") val sender: String,
    @ColumnInfo(name = "quizId") val quizId: Int?,
    @ColumnInfo(name = "groupId") val groupId: Int?,
    @ColumnInfo(name = "noteId") val noteId: Int?,
    @ColumnInfo(name = "receiver") val receiver: Int,
    @ColumnInfo(name = "syncId") val syncId: Long? = null
)