package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.ActivityType

interface ActivityTypesRepository {
    suspend fun fetchAll(): List<ActivityType>
}