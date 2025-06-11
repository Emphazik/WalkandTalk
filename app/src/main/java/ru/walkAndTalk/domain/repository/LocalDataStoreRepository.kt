package ru.walkAndTalk.domain.repository

import kotlinx.coroutines.flow.Flow

interface LocalDataStoreRepository {
    val isFirstLaunch: Flow<Boolean>
    val accessToken: Flow<String>
    val userMode: Flow<String> // Добавляем userMode

    suspend fun saveIsFirstLaunch(value: Boolean)
    suspend fun saveAccessToken(value: String)
    suspend fun saveUserMode(value: String) // Добавляем метод для сохранения режима
}