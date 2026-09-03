package com.example.diplomski.websocket

import android.util.Log
import com.example.diplomski.retrofit.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketManager {

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .pingInterval(0, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)   // ← bez timeout-a za čitanje
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _events =
        MutableSharedFlow<WebSocketEvent>(
            extraBufferCapacity = 64
        )

    val events: SharedFlow<WebSocketEvent> =
        _events

    private var userId: Long = -1

    fun connect(userId: Long) {
        this.userId = userId
        if (webSocket != null) return
        createConnection(userId)
    }

    private fun createConnection(userId: Long) {
        val url = if (RetrofitClient.isEmulator()) {
            "ws://10.0.2.2:8080/ws?userId=$userId"
        } else {
            "ws://127.0.0.1:8080/ws?userId=$userId"
        }

        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WEBSOCKET", "Connected successfully!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    Log.d("WEBSOCKET", "Message received: $text")
                    val event = gson.fromJson(text, WebSocketEvent::class.java)
                    _events.tryEmit(event)
                } catch (e: Exception) {
                    Log.e("WEBSOCKET", "Parse error", e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WEBSOCKET", "Failed: ${t.message}")
                this@WebSocketManager.webSocket = null

                // reconnect nakon 5 sekundi
                if (this@WebSocketManager.userId != -1L) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        Log.d("WEBSOCKET", "Reconnecting...")
                        createConnection(this@WebSocketManager.userId)
                    }, 5000)
                }
            }



            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WEBSOCKET", "Closed: $reason")
                this@WebSocketManager.webSocket = null
            }
        })
    }

    fun disconnect() {

        webSocket?.close(
            1000,
            "User disconnected"
        )

        webSocket = null
    }
}