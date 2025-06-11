package ru.walkAndTalk.domain.model

data class Report(
    val id: String,
    val contentType: String, // 'event', 'announcement', 'user'
    val contentId: String,
    val userId: String,
    val reason: String,
    val createdAt: String
)