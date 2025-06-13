package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventStatusDto(
    val id: Long,
    val name: String
)