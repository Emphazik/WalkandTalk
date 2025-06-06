package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.domain.model.Message

fun MessageDto.toDomain(senderName: String? = null): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        createdAt = createdAt,
        isRead = isRead,
        tempId = tempId,
        deletedBy = deletedBy,
        senderName = senderName
    )
}