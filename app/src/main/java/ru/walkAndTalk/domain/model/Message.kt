package ru.walkAndTalk.domain.model

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val isRead: Boolean,
    val tempId: String? = null,
    val deletedBy: List<String>? = null,
    val senderName: String? = null // Добавляем поле
)