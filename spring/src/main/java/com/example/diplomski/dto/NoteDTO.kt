package com.example.diplomski.dto

data class NoteDto(
    val id: Long = 0,
    val title: String,
    val path: String,
    val type: String,
    val folderId: Long?
)