package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Interest

interface UserInterestsRepository {
    suspend fun fetchInterestsForUser(userId: String): List<Interest>
    suspend fun addInterest(userId: String, interestId: String)
    suspend fun removeInterest(userId: String, interestId: String)
}