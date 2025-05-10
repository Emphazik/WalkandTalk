package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.CityKnowledgeLevelDto
import ru.walkAndTalk.domain.model.CityKnowledgeLevel

fun CityKnowledgeLevelDto.toDomain(): CityKnowledgeLevel {
    return CityKnowledgeLevel(
        id = id,
        name = name
    )
}