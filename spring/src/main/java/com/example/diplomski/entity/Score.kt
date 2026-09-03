package com.example.diplomski.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "score")
data class Score(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val score: Int,
    @ManyToOne @JoinColumn(name = "userId")
    val user: User,
    @ManyToOne @JoinColumn(name = "quizId")
    val quiz: Quiz
)