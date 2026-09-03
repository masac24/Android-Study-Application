package com.example.diplomski.service

import com.example.diplomski.entity.User
import com.example.diplomski.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(username: String, password: String): User {
        if (userRepository.findByUsername(username) != null) {
            throw RuntimeException("Username already exists")
        }
        return userRepository.save(
            User(username = username, password = passwordEncoder.encode(password)!!)
        )
    }

    fun authenticate(username: String, password: String): User? {
        val user = userRepository.findByUsername(username) ?: return null
        if (!passwordEncoder.matches(password, user.password)) return null
        return user
    }
}