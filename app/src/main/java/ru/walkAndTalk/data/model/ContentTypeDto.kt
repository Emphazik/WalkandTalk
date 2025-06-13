package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentTypeDto(
    val id: Long,
    val name: String
)