package ru.walkAndTalk.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import ru.walkAndTalk.R

@Serializable
data class Event(
    val id: String,
    @SerialName("creator_id") val creatorId: String,
    val title: String,
    val description: String,
    val location: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("route_id") val routeId: String? = null,

    // Добавляем поле для изображения (заглушка)
    val image: @Composable () -> @Contextual Painter = { painterResource(id = R.drawable.default_event_image) }

)