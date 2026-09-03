package com.example.diplomski.retrofit.dto

data class SessionDto(
    val id: Long = 0,
    val time: Int,
    val datetime: Long,
    val completed: Boolean,
    val userId: Long
)