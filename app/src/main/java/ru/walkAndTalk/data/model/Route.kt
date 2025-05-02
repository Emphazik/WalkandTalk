package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: String,
    @SerialName("creator_id") val creatorId: String,
    val title: String,
    val description: String,
    val path: String, // JSONB, храним как строку
    @SerialName("created_at") val createdAt: String? = null
)