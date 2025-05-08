package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    @SerialName("id") val id: String,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("path") val path: String,
    @SerialName("created_at") val createdAt: String? = null
)