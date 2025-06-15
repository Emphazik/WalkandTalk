package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ActivityTypeDto
import ru.walkAndTalk.domain.model.ActivityType

fun ActivityTypeDto.toDomain(): ActivityType {
    return ActivityType(
        id = id,
        name = name
    )
}