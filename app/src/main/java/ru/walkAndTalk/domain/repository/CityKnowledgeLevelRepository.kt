package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.CityKnowledgeLevel

interface CityKnowledgeLevelRepository {
    suspend fun fetchById(id: String): CityKnowledgeLevel?
    suspend fun fetchAll(): List<CityKnowledgeLevel>
}