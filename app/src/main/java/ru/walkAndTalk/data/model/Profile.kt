package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Profile(
    @SerialName("user_id") val userId: String,
    @SerialName("interest_id") val interestId: String,
    @SerialName("city_knowledge_level_id") val cityKnowledgeLevelId: String,
    val goals: String? = null,
    val bio: String? = null,
    @SerialName("events_attended") val eventsAttended: Int? = null
)