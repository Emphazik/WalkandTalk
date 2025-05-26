package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.domain.model.ChatParticipant

fun ChatParticipantDto.toDomain(): ChatParticipant {
    return ChatParticipant(
        chatId = chatId,
        userId = userId,
        lastReadAt = lastReadAt

    )
}