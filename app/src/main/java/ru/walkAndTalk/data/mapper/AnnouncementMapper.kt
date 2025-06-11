package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.domain.model.Announcement

fun AnnouncementDto.toDomain(statusName: String = "pending"): Announcement {
    return Announcement(
        id = id,
        title = title,
        description = description,
        creatorId = creatorId,
        activityType = activityType,
        status = statusName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Announcement.toDto(statusId: String): AnnouncementDto {
    return AnnouncementDto(
        id = id,
        title = title,
        description = description,
        creatorId = creatorId,
        activityType = activityType,
        statusId = statusId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}