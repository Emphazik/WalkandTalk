package ru.walkAndTalk.domain.model

sealed interface FeedItem {
    val id: String
    val title: String
    val description: String?
    val creatorId: String
    val createdAt: String
    val statusId: String
    val location: String?
    val type: FeedItemType
}

enum class FeedItemType {
    EVENT, ANNOUNCEMENT
}