package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.domain.model.Event

fun EventDto.toDomain(): Event {
    return Event(
        id = id,
        creatorId = creatorId,
        title = title,
        description = description,
        location = location,
        eventDate = eventDate,
        createdAt = createdAt.toString(),
        imageUrl = image.toString()
    )
}