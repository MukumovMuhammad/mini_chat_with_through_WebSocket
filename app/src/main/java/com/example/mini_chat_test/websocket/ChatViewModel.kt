package com.example.mini_chat_test.websocket

import AppWebSocketListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {
    private val serverUrl = "ws://192.168.123.40:8080/ws/232131"
    private var webSocketClient : WebSocketClient? = null

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private val _status = MutableStateFlow("Disconnected")
    val status: StateFlow<String> = _status

    init {
        webSocketClient = WebSocketClient(serverUrl, AppWebSocketListener(::onMessageReceived, ::onStatusChanged))
        connect()
    }

    private fun connect(){
        viewModelScope.launch {
            viewModelScope.launch {
                _status.value = "Connecting..."
                webSocketClient?.connect()
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isNotBlank()) {
            webSocketClient?.sendMessage(message)
        }
    }

    private fun onMessageReceived(message: String) {
        // Update UI state on the main thread
        viewModelScope.launch {
            _messages.value = _messages.value + message
        }
    }

    private fun onStatusChanged(newStatus: String) {
        viewModelScope.launch {
            _status.value = newStatus
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient?.disconnect()
    }



}