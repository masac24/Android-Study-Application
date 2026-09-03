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
@Table(name = "question")
data class Question(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val text: String,
    val answer: String,
    val type: String,
    val option1: String,
    val option2: String,
    val option3: String,
    @ManyToOne @JoinColumn(name = "quizId")
    val quiz: Quiz
)