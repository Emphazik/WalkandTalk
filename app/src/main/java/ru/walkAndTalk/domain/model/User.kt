package ru.walkAndTalk.domain.model

data class User(
    val id: String,
    val email: String,
    val phone: String,
    val name: String,
    val profileImageUrl: String,
    val vkId: String
)
