package com.example.diplomski.database.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "note", foreignKeys = [
    ForeignKey(
        entity = Folder::class,
        parentColumns = ["id"],
        childColumns = ["folderId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
])
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "folderId") val folderId: Int?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "syncId") val syncId: Long? = null,
    @ColumnInfo(name = "serverPath") val serverPath: String? = null
    )
