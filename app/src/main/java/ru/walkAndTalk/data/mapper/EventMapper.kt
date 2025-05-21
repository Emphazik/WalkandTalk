package ru.walkAndTalk.data.mapper

import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.domain.model.Event

fun EventDto.toDomain(tagNames: List<String> = emptyList()): Event {
    return Event(
        id = id,
        creatorId = creatorId,
        title = title,
        description = description,
        location = location,
        eventDate = eventDate,
        createdAt = createdAt ?: Clock.System.now().toString(),
        eventImageUrl = eventImageUrl,
        tagIds = tagNames
    )
}