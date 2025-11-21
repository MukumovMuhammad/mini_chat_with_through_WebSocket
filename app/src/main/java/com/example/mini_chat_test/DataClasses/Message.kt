package com.example.mini_chat_test.DataClasses

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String,
    val username: String,
    val from: Int
)