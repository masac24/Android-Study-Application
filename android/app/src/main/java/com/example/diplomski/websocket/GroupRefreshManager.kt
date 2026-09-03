package com.example.diplomski.websocket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GroupRefreshManager {

    private val _refreshEvents =
        MutableSharedFlow<Long>(
            replay = 0,
            extraBufferCapacity = 1
        )

    val refreshEvents =
        _refreshEvents.asSharedFlow()

    fun refresh(serverUserId: Long) {
        _refreshEvents.tryEmit(serverUserId)
    }
}