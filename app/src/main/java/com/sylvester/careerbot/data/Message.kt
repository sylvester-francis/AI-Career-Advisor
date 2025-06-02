package com.sylvester.careerbot.data

data class Message(
    val content: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    USER,
    BOT
}
