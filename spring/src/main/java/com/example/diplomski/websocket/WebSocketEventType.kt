package com.example.diplomski.websocket


data class WebSocketEvent(
    val type: String,
    val groupId: Long? = null,
    val userId: Long? = null,
    val entityId: Long? = null,
    val message: String? = null
)