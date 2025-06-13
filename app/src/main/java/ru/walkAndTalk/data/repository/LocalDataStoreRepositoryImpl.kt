package ru.walkAndTalk.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository

class LocalDataStoreRepositoryImpl(
    private val context: Context
) : LocalDataStoreRepository {

    companion object {
        private const val PREFERENCES_NAME = "settings"
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

        private const val IS_FIRST_LAUNCH_KEY_NAME = "is_first_launch"
        val IS_FIRST_LAUNCH_KEY = booleanPreferencesKey(IS_FIRST_LAUNCH_KEY_NAME)

        private const val ACCESS_TOKEN_KEY_NAME = "access_token"
        val ACCESS_TOKEN_KEY = stringPreferencesKey(ACCESS_TOKEN_KEY_NAME)

        private const val USER_MODE_KEY_NAME = "user_mode"
        val USER_MODE_KEY = stringPreferencesKey(USER_MODE_KEY_NAME)
    }

    override val isFirstLaunch = context.dataStore.data.map {
        it[IS_FIRST_LAUNCH_KEY] ?: true
    }

    override val accessToken = context.dataStore.data.map {
        it[ACCESS_TOKEN_KEY] ?: ""
    }

    override val userMode = context.dataStore.data.map {
        it[USER_MODE_KEY] ?: "user"
    }

    override suspend fun saveIsFirstLaunch(value: Boolean) {
        context.dataStore.edit {
            it[IS_FIRST_LAUNCH_KEY] = value
        }
    }

    override suspend fun saveAccessToken(value: String) {
        context.dataStore.edit {
            it[ACCESS_TOKEN_KEY] = value
        }
    }

    override suspend fun saveUserMode(value: String) {
        context.dataStore.edit {
            it[USER_MODE_KEY] = value
        }
    }
}
