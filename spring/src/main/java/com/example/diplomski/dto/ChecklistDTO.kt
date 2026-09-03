package com.example.diplomski.dto

data class ChecklistDto(
    val id: Long = 0,
    val text: String,
    val done: Boolean,
    val userId: Long
)