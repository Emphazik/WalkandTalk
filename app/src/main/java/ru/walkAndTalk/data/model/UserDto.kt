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
    @SerialName("password")
    val password: String,
    @SerialName("profile_image_url")
    val profileImageUrl: String,
    @SerialName("vk_id")
    val vkId: Long? = null,
    @SerialName("interest_ids")
    val interestIds: List<String> = emptyList(),
    @SerialName("city_knowledge_level_id")
    val cityKnowledgeLevelId: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("goals")
    val goals: String? = null,
    @SerialName("created_at")
    val createdAt: String, // Instant будет сериализоваться как строка
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("birthdate")
    val birthdate: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("show_reviews")
    val showReviews: Boolean = false,
    @SerialName("is_admin") val isAdmin: Boolean = false
)
