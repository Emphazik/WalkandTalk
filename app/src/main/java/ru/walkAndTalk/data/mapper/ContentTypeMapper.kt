package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ContentTypeDto
import ru.walkAndTalk.domain.model.ContentType

fun ContentTypeDto.toDomain(): ContentType {
    return ContentType(
        id = id,
        name = name
    )
}

fun ContentType.toDto(): ContentTypeDto {
    return ContentTypeDto(
        id = id,
        name = name
    )
}