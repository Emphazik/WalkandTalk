package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Event(
    val id: String,
    @SerialName("creator_id") val creatorId: String,
    val title: String,
    val description: String,
    val location: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("route_id") val routeId: String? = null
)