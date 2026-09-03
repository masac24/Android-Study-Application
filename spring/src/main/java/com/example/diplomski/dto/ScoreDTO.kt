package com.example.diplomski.dto

data class ScoreDto(
    val id: Long = 0,
    val score: Int,
    val userId: Long,
    val quizId: Long
)