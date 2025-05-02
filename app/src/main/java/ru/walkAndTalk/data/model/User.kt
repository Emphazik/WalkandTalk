package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User(
    val id: String,
    val email: String,
    val phone: String,
    val name: String,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("vk_id") val vkId: String? = null,
    @SerialName("password_hash") val passwordHash: String? = null
)
