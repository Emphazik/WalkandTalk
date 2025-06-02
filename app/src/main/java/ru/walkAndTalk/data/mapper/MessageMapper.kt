package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.domain.model.Message

fun MessageDto.toDomain(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        createdAt = createdAt,
        isRead = isRead,
        tempId = null, // Поле отсутствует в базе
        deletedBy = deletedBy
    )
}