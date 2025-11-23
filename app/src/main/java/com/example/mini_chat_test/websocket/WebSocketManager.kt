package com.example.mini_chat_test.websocket

// Create a new file: WebSocketManager.kt
import AppWebSocketListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object WebSocketManager {

    private const val WEB_SOCKET_URL = "wss://mini-chat-service-1091763228160.europe-west1.run.app/ws?user_id="
    private var webSocketClient: WebSocketClient? = null


    // Use a custom scope that won't be cancelled with a ViewModel
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Expose connection status
    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus = _connectionStatus.asStateFlow()

    // Expose incoming messages
    private val _messages = MutableStateFlow<String?>(null)
    val messages = _messages.asStateFlow()

    fun startConnection(userId: Int) {
        if (webSocketClient == null || connectionStatus.value != "Connected") {
            Log.i("WebSocketManager_TAG", "Initializing and connecting WebSocket for user: $userId")
            val listener = AppWebSocketListener(
                onMessage = { message ->
                    scope.launch {
                        _messages.value = message
                    }
                },
                onStatus = { newStatus ->
                    scope.launch {
                        Log.i("WebSocketManager_TAG", "New status has been got in manager ${newStatus}")
                        _connectionStatus.value = newStatus
                        if (newStatus == "Closed" || newStatus == "Failed" || newStatus == "Failed: null") {
                            Log.e("WebSocket", "Disconnected, trying to reconnect...")
                            startConnection(userId)
                        }
                    }
                }
            )
            webSocketClient = WebSocketClient(WEB_SOCKET_URL + userId, listener)
            webSocketClient?.connect()
        } else {
            Log.w("WebSocketManager_TAG", "Connection already active or in progress.")
        }
    }

    fun sendMessage(message: String) {
        if (connectionStatus.value == "Connected") {
            webSocketClient?.sendMessage(message)
        } else {
            Log.e("WebSocketManager_TAG", "Cannot send message, WebSocket is not connected.")
        }
    }

    fun CheckConnectionStatus(){
        Log.e("WebSocketManager_TAG", "Status is ${connectionStatus.value}")
    }

    fun closeConnection() {
        Log.i("WebSocketManager_TAG", "Closing WebSocket connection.")
        webSocketClient?.disconnect()
        webSocketClient = null // Clear the instance
        _connectionStatus.value = "Closed"
    }
}
