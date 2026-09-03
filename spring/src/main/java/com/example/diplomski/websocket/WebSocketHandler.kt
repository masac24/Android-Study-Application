package com.example.diplomski.websocket

import com.google.gson.Gson
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class AppWebSocketHandler : TextWebSocketHandler() {

    private val gson = Gson()

    private val sessions =
        ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    override fun afterConnectionEstablished(
        session: WebSocketSession
    ) {
        println("=== WebSocket connection attempt ===")
        val query = session.uri?.query ?: run {
            println("No query params")
            return
        }
        println("Query: $query")

        val userId = query
            .split("&")
            .firstOrNull {
                it.startsWith("userId=")
            }
            ?.substringAfter("=")
            ?.toLongOrNull()
            ?: return

        sessions
            .getOrPut(userId) {
                ConcurrentHashMap.newKeySet()
            }
            .add(session)

        println("WebSocket connected: user $userId")
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ) {
        sessions.values.forEach { userSessions ->
            userSessions.remove(session)
        }
    }

    fun sendToUser(
        userId: Long,
        event: WebSocketEvent
    ) {
        val json = gson.toJson(event)

        sessions[userId]?.forEach { session ->

            try {
                if (session.isOpen) {
                    session.sendMessage(
                        TextMessage(json)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendToUsers(
        userIds: List<Long>,
        event: WebSocketEvent
    ) {
        userIds.forEach {
            sendToUser(it, event)
        }
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        when (message) {
            is PingMessage -> {
                session.sendMessage(PongMessage(message.payload))
            }
            is TextMessage -> {
                handleTextMessage(session, message)
            }
        }
    }

}