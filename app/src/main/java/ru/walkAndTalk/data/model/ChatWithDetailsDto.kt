package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatWithDetailsDto(
    val chat: ChatDto,
    val participants: List<ChatParticipantDto>,
    val currentParticipant: ChatParticipantDto? = null, // Сделать nullable
    val lastMessage: MessageDto? = null, // Сделать nullable
    val users: List<UserDto>
)