package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id") val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("content") val content: String,
    @SerialName("sent_at") val sentAt: String
)