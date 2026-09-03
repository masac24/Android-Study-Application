package com.example.diplomski

import com.example.diplomski.websocket.AppWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val appWebSocketHandler: AppWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(appWebSocketHandler, "/ws")
            .setAllowedOriginPatterns("*")
            .setAllowedOrigins("*")  // ← dodaj
    }
}