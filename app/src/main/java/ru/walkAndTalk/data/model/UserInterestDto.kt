package ru.walkAndTalk.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInterestDto(
    @SerialName("user_id") val userId: String,
    @SerialName("interest_id") val interestId: String
)