package com.example.diplomski.controller

import com.example.diplomski.dto.AuthRequest
import com.example.diplomski.dto.AuthResponse
import com.example.diplomski.service.JwtService
import com.example.diplomski.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService
) {
    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val user = userService.register(request.username, request.password)
        val token = jwtService.generateToken(user)
        return ResponseEntity.ok(AuthResponse(token, user.id))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val user = userService.authenticate(request.username, request.password)
            ?: return ResponseEntity.status(401).build()
        val token = jwtService.generateToken(user)
        return ResponseEntity.ok(AuthResponse(token, user.id))
    }
}