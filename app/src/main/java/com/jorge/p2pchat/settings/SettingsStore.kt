package com.jorge.p2pchat.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Ajustes del usuario. Nota de diseño: SHARE_ACTIVITY_COUNT queda en false
 * por defecto (opt-in, no opt-out) — el usuario decide activamente mostrar
 * su conteo de chats/grupos al admin, en vez de tener que encontrar dónde
 * apagarlo.
 */
class SettingsStore(private val context: Context) {

    companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SHARE_ACTIVITY_COUNT = booleanPreferencesKey("share_activity_count")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[DARK_THEME] ?: false }
    val shareActivityCount: Flow<Boolean> = context.dataStore.data.map { it[SHARE_ACTIVITY_COUNT] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun setShareActivityCount(enabled: Boolean) {
        context.dataStore.edit { it[SHARE_ACTIVITY_COUNT] = enabled }
    }
}
