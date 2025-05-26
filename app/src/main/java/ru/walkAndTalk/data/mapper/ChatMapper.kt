package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.Event

fun ChatDto.toDomain(
    event: Event? = null,
    participantName: String? = null,
    participantIds: List<String> = emptyList(), // Добавляем параметр
    lastMessage: String? = null,
    lastMessageTime: String? = null,
    unreadCount: Int? = null
): Chat {
    return Chat(
        id = id,
        type = type,
        eventId = eventId,
        eventName = event?.title,
        participantIds = participantIds,
        participantName = participantName,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount
    )
}
