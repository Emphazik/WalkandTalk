package ru.walkAndTalk.domain.repository

import kotlinx.coroutines.flow.Flow

interface LocalDataStoreRepository {
    val accessToken: Flow<String>

    suspend fun saveAccessToken(accessToken: String)
}