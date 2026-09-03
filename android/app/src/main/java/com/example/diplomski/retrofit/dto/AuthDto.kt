package com.example.diplomski.retrofit.dto

data class AuthRequest(val username: String, val password: String)

data class AuthResponse(val token: String, val userId: Long)
