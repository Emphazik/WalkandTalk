package ru.walkAndTalk.domain.model

data class Announcement(
    override val id: String,
    override val title: String,
    override val description: String,
    override val creatorId: String,
    val activityTypeId: String,
    override val statusId: String,
    override val createdAt: String,
    val updatedAt: String?,
    val organizerName: String? = null,
    override val location: String,
    val imageUrl: String? = null
) : FeedItem {
    override val type: FeedItemType = FeedItemType.ANNOUNCEMENT
}