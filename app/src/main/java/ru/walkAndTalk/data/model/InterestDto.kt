package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterestDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String
)