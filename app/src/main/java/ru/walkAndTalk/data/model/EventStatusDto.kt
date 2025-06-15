package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventStatusDto(
    val id: String,
    val name: String
)