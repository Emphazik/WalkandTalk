package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.domain.model.Announcement

fun Announcement.toDto(): AnnouncementDto {
    return AnnouncementDto(
        id = id,
        title = title,
        description = description,
        creatorId = creatorId,
        activityTypeId = activityTypeId,
        statusId = statusId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        location = location,
        imageUrl = imageUrl
    )
}

fun AnnouncementDto.toDomain(): Announcement {
    return Announcement(
        id = id,
        title = title,
        description = description,
        creatorId = creatorId,
        activityTypeId = activityTypeId,
        statusId = statusId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        location = location,
        imageUrl = imageUrl
    )
}