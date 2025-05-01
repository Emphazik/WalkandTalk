package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String, // UUID
    val email: String,
    val phone: String,
    val name: String,
    val profileImageUrl: String? = null,
    val vkId: String
)