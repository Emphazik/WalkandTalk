package ru.walkAndTalk.domain.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val email: String,
    val phone: String,
    val name: String,
    val profileImageUrl: String,
    val vkId: Long?,
    val interestIds: List<String> = emptyList(),
    val cityKnowledgeLevelId: String?, // UUID как строка
    val bio: String?,
    val goals: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
)
