package com.example.diplomski.retrofit.dto

data class CommentDto(val id : Long = 0, val text: String,
    val userId : Long, val groupId: Long, val quizId: Long?, val noteId: Long?, val createdAt : Long)

