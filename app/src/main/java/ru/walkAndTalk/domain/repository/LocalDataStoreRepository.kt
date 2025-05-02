package ru.walkAndTalk.domain.repository

import kotlinx.coroutines.flow.Flow

interface LocalDataStoreRepository {
    val isFirstLaunch: Flow<Boolean>
    val accessToken: Flow<String>

    suspend fun saveIsFirstLaunch(value: Boolean)
    suspend fun saveAccessToken(value: String)
}