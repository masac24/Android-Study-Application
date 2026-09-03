package com.example.diplomski.retrofit.dto

data class MessageDto(
    val id: Long = 0, val sender: String, val quizId: Long?, val groupId: Long?, val noteId: Long?,
    val receiver: Long
) {
}