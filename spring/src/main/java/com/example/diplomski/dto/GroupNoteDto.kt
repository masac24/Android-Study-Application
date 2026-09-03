package com.example.diplomski.dto

data class GroupNoteDto(
    val id: Long = 0,
    val noteId: Long,
    val groupId: Long,
    val username: String
)