package com.chronodisrup.chronoplay.core.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.chronoDataStore by preferencesDataStore(name = "chrono_play_settings")

class SettingsStore(private val context: Context) {
    private val serverKey = stringPreferencesKey("muriel_server_url")
    private val tokenKey = stringPreferencesKey("muriel_server_token")
    val serverUrl: Flow<String> = context.chronoDataStore.data.map { it[serverKey] ?: "" }
    val serverToken: Flow<String> = context.chronoDataStore.data.map { it[tokenKey] ?: "hellozhou" }

    suspend fun saveConnection(url: String, token: String) {
        context.chronoDataStore.edit {
            it[serverKey] = url.trim().trimEnd('/') + "/"
            it[tokenKey] = token.trim()
        }
    }
}