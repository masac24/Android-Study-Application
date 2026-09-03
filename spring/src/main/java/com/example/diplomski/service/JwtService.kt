package com.example.diplomski.service

import com.example.diplomski.entity.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService {

    private val secretKey = Keys.hmacShaKeyFor(
        "diplomski-secret-key-mora-biti-dugacak-bar-32-karaktera".toByteArray()
    )

    fun generateToken(user: User): String {
        return Jwts.builder()
            .subject(user.username)
            .claim("userId", user.id)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 86400000)) // 24h
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: Exception) { null }
    }

    fun isTokenValid(token: String): Boolean {
        return extractUsername(token) != null
    }
}