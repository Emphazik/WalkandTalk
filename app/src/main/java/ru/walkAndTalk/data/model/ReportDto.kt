package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportDto(
    @SerialName("id") val id: String,
    @SerialName("content_type_id") val contentTypeId: String,
    @SerialName("content_id") val contentId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("reason") val reason: String,
    @SerialName("created_at") val createdAt: String
)