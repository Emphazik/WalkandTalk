package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnnouncementDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("activity_type_id") val activityTypeId: String,
    @SerialName("status_id") val statusId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("location") val location: String,
    @SerialName("image_url") val imageUrl: String? = null
)