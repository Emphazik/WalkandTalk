package ru.walkAndTalk.domain.model

data class ChatParticipant(
    val chatId: String,
    val userId: String,
    val lastReadAt: String? = null,
    val isMuted: Boolean = false, // Новое поле
    val lastSeen: String? = null // Новое поле
)