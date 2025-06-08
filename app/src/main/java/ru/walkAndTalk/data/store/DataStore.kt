package ru.walkAndTalk.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

object UserPreferences {
    private val HAS_SEEN_PROFILE_NOTIFICATION = booleanPreferencesKey("has_seen_profile_notification")

    suspend fun hasSeenProfileNotification(context: Context): Boolean {
        return context.dataStore.data
            .map { preferences -> preferences[HAS_SEEN_PROFILE_NOTIFICATION] ?: false }
            .first()
    }

    suspend fun setHasSeenProfileNotification(context: Context, hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_PROFILE_NOTIFICATION] = hasSeen
        }
    }
}