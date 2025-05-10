package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.InterestDto
import ru.walkAndTalk.domain.model.Interest

fun InterestDto.toDomain(): Interest {
    return Interest(
        id = id,
        name = name
    )
}