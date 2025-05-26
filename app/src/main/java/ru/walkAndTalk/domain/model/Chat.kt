package ru.walkAndTalk.domain.model

data class Chat(
    val id: String,
    val type: String, // "group" или "private"
    val eventId: String? = null,
    val eventName: String? = null, // Для групповых
    val participantIds: List<String> = emptyList(),
    val participantName: String? = null, // Для личных (имя второго участника)
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int?
)