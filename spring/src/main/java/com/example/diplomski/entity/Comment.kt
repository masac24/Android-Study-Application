package com.example.diplomski.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "comment")
data class Comment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val text: String,
    val createdAt: Long,
    @ManyToOne @JoinColumn(name = "userId")
    val user: User,
    @ManyToOne @JoinColumn(name = "groupId")
    val group: Group,
    @ManyToOne @JoinColumn(name = "quizId")
    val quiz: Quiz?,
    @ManyToOne @JoinColumn(name = "noteId")
    val note: Note?,
)