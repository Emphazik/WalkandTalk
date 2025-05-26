package ru.walkAndTalk.domain.model

data class ChatParticipant(
    val chatId: String,
    val userId: String,
    val lastReadAt: String? = null
)