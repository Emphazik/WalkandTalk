package ru.walkAndTalk.domain.model

data class Chat(
    val id: String,
    val type: String, // "group" или "private"
    val eventId: String? = null,
    val eventName: String? = null, // Для групповых
    val participantIds: List<String> = emptyList(),
    val participantName: String? = null, // Для личных (имя второго участника)
    val participantUser: User?, // Добавляем объект User для собеседника
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int?,
    val isMessageRead: Boolean?,// Добавляем поле для статуса прочтения
    val isMuted: Boolean = false // Будет зависеть от текущего пользователя
)