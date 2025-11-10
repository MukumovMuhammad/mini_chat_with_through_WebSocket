package com.example.mini_chat_test.websocket

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val serverUrl: String,
    private val listener: WebSocketListener
) {

    private var webSocket: WebSocket? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    fun connect(){
        val request = okhttp3.Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, listener)
        Log.i("WebSocket_TAG", "Connected")
    }

    fun sendMessage(message: String): Boolean{
        return webSocket?.send(message) ?: false
    }

    fun disconnect(){
        webSocket?.close(1000, null)
       client.dispatcher.executorService.shutdown()
    }
}
