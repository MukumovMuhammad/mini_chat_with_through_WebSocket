package com.example.mini_chat_test

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val message: String,
    val client_id: String
)
