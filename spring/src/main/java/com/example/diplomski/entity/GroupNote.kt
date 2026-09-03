package com.example.diplomski.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "groupnote")
data class GroupNote(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne @JoinColumn(name = "noteId")
    val note: Note,
    @ManyToOne @JoinColumn(name = "groupId")
    val group: Group,
    @ManyToOne @JoinColumn(name = "userId")
    val user: User
)