package ru.walkAndTalk.domain.model

//data class Event(
//    val id: String,
//    val creatorId: String,
//    val title: String,
//    val description: String,
//    val location: String,
//    val eventDate: String,
//    val createdAt: String,
//    val eventImageUrl: String?,
//    //OrganizerName v bd nety, local
//    val organizerName: String? = null,
//    val tagIds: List<String> = emptyList(),
//    val status: String,// 'pending', 'approved', 'rejected', 'reported'
//)

data class Event(
    override val id: String,
    override val creatorId: String,
    override val title: String,
    override val description: String,
    override val location: String,
    val eventDate: String,
    override val createdAt: String,
    val eventImageUrl: String?,
    val organizerName: String? = null,
    val tagIds: List<String>,
    override val statusId: String,
    val maxParticipants: Int? = null // New field for participant limit
) : FeedItem {
    override val type: FeedItemType = FeedItemType.EVENT
}