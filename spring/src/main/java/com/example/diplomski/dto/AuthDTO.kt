package com.example.diplomski.dto

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val userId: Long
)