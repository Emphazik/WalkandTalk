package ru.walkAndTalk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CityKnowledgeLevel(

    val id: String,
    val name: String // новичек, продвинутый, гуляка.
)
