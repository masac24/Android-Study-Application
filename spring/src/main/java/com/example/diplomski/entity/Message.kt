package com.example.diplomski.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "message")
data class Message(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne @JoinColumn(name = "sender_id")
    val sender: User,

    @ManyToOne @JoinColumn(name = "receiver_id")
    val receiver: User,

    @ManyToOne @JoinColumn(name = "quiz_id")
    val quiz: Quiz?,

    @ManyToOne @JoinColumn(name = "group_id")
    val group: Group?,

@ManyToOne @JoinColumn(name = "note_id")
    val note: Note?
)