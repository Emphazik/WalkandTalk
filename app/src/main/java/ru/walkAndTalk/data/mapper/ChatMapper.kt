package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.User

fun ChatDto.toDomain(
    event: Event? = null,
    participantName: String? = null,
    participantIds: List<String> = emptyList(), // Хранение [uuid]
    participantUser: User? = null, // Добавляем параметр пользователя
    lastMessage: String? = null,
    lastMessageTime: String? = null,
    unreadCount: Int? = null,
    isMessageRead: Boolean? = null,
    isMuted: Boolean = false // Уведы с сообщений
): Chat {
    return Chat(
        id = id,
        type = type,
        eventId = eventId,
        eventName = event?.title,
        participantIds = participantIds,
        participantName = participantName,
        participantUser = participantUser,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        isMessageRead = isMessageRead,
        isMuted = isMuted
    )
}
