package org.swirlsea.tiletalk.data

import okhttp3.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class WebSocketClient(private val client: OkHttpClient) {

    private var webSocket: WebSocket? = null
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WebSocket Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {

            CoroutineScope(Dispatchers.IO).launch {
                _messages.emit(text)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            println("WebSocket Closing: $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WebSocket Error: " + t.message)
        }
    }

    fun connect(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}