package ru.walkAndTalk.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository

class LocalDataStoreRepositoryImpl(private val context: Context) : LocalDataStoreRepository {

    companion object {

        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        private const val ACCESS_TOKEN_KEY_NAME = "access_token"
        val ACCESS_TOKEN_KEY = stringPreferencesKey(ACCESS_TOKEN_KEY_NAME)
    }

    override val accessToken = context.dataStore.data.map {
        it[ACCESS_TOKEN_KEY] ?: ""
    }

    override suspend fun saveAccessToken(accessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }
}