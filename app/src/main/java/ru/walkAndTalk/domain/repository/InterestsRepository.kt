package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Interest

interface InterestsRepository {
    suspend fun fetchById(id: String): Interest?
    suspend fun fetchAll(): List<Interest>
}