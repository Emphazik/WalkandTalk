package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatDtoWithRelations(
    @SerialName("chat") val chat: ChatDto,
    @SerialName("chat_participants") val chatParticipants: List<ChatParticipantDto>,
    @SerialName("messages") val messages: List<MessageDto>,
    @SerialName("events") val events: EventDto? = null
)