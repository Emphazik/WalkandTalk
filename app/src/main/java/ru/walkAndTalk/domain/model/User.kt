package ru.walkAndTalk.domain.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val email: String,
    val phone: String,
    val name: String,
    val password: String,
    val profileImageUrl: String,
    val vkId: Long? = null,
    val interestIds: List<String> = emptyList(),
    val cityKnowledgeLevelId: String? = null, // UUID как строка
    val bio: String? = null,
    val goals: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val birthdate: String? = null,
    val city: String? = null,
    val showReviews: Boolean = false,
    val isAdmin: Boolean = false,
    val gender: String? = null, // Новое поле: "male", "female", "other"
    val lastLogin: Instant? = null // Новое поле
)
