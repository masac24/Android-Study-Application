package com.example.diplomski.dto

data class CommentDto(val id : Long = 0, val text: String,
                      val userId : Long, val groupId: Long, val quizId: Long?, val noteId: Long?,
                      val createdAt : Long)
