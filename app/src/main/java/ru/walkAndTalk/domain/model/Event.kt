package ru.walkAndTalk.domain.model

data class Event(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String,
    val location: String,
    val eventDate: String,
    val createdAt: String,
    val eventImageUrl: String?,
    //OrganizerName v bd nety, local
    val organizerName: String? = null,
    val tagIds: List<String> = emptyList(),
    val status: String // 'pending', 'approved', 'rejected', 'reported'
)