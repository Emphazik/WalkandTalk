package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Interest(
    val id: String,
    val name: String
)