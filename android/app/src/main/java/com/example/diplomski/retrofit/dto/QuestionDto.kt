package com.example.diplomski.retrofit.dto

data class QuestionDto(
    val id: Long = 0,
    val text: String,
    val answer: String,
    val type: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val quizId: Long
)