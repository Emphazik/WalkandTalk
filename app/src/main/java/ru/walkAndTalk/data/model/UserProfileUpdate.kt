package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserProfileUpdate(
    @SerialName("name")
    val name: String? = null,
    @SerialName("birthdate")
    val birthDate: String? = null,
    @SerialName("profile_image_url")
    val profileImageUrl: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("goals")
    val goals: String? = null,
    @SerialName("city")
    val city: String? = null
)