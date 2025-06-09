package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.NotificationDto
import ru.walkAndTalk.domain.model.Notification
import ru.walkAndTalk.domain.model.NotificationType

fun toDomain(dto: NotificationDto): Notification {
    return Notification(
        id = dto.id,
        userId = dto.userId,
        type = NotificationType.entries.firstOrNull { it.name == dto.type } ?: NotificationType.NewEvent, // Значение по умолчанию
        content = dto.content,
        createdAt = dto.createdAt,
        isRead = dto.isRead,
        relatedId = dto.relatedId
    )
}

fun toDto(notification: Notification): NotificationDto {
    return NotificationDto(
        id = notification.id,
        userId = notification.userId,
        type = notification.type.name,
        content = notification.content,
        createdAt = notification.createdAt,
        isRead = notification.isRead,
        relatedId = notification.relatedId
    )
}
