package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,
    @SerialName("email")
    val email: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("name")
    val name: String,
    @SerialName("profile_image_url")
    val profileImageUrl: String,
    @SerialName("vk_id")
    val vkId: Long? = null,
    @SerialName("password_hash")
    val passwordHash: String? = null
)
