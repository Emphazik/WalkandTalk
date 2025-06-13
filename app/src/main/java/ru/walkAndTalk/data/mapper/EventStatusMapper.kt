package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.EventStatusDto
import ru.walkAndTalk.domain.model.EventStatus

fun EventStatusDto.toDomain(): EventStatus {
    return EventStatus(
        id = id,
        name = name
    )
}

fun EventStatus.toDto(): EventStatusDto {
    return EventStatusDto(
        id = id,
        name = name
    )
}