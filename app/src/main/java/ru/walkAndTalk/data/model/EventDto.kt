package ru.walkAndTalk.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import ru.walkAndTalk.R

@Serializable
data class EventDto(
    @SerialName("id") val id: String,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("location") val location: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("route_id") val routeId: String? = null,
    @SerialName("event_image_url") val eventImageUrl: String? = null,
)