package ru.walkAndTalk.domain.model

data class Announcement(
    val id: String,
    val title: String,
    val description: String?,
    val creatorId: String,
    val activityType: String,
    val status: String, // 'pending', 'approved', 'rejected', 'reported'
    val createdAt: String,
    val updatedAt: String?
)