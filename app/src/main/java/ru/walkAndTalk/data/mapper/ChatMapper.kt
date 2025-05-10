package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.Event

fun ChatDto.toDomain(event: Event): Chat {
    return Chat(
        id = id,
        eventId = eventId,
        eventName = event?.title ?: "Unknown Event",
        lastMessage = lastMessage.toString(),
        lastMessageTime = lastMessageTime.toString(),
        unreadCount = unreadCount
    )
}