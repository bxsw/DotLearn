package com.dotlearn.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dotlearn_settings"
)

/** 应用设置（DataStore）。 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val POMODORO = intPreferencesKey("pomodoro_minutes")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val THEME = intPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val pomodoroMinutes: Flow<Int> =
        context.settingsDataStore.data.map { it[Keys.POMODORO] ?: 25 }

    val reminderEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[Keys.REMINDER_ENABLED] ?: false }

    val reminderHour: Flow<Int> =
        context.settingsDataStore.data.map { it[Keys.REMINDER_HOUR] ?: 20 }

    val reminderMinute: Flow<Int> =
        context.settingsDataStore.data.map { it[Keys.REMINDER_MINUTE] ?: 0 }

    /** 0 跟随系统 / 1 浅色 / 2 深色 */
    val themeMode: Flow<Int> =
        context.settingsDataStore.data.map { it[Keys.THEME] ?: 0 }

    /** "" 跟随系统 / "zh" / "en" */
    val language: Flow<String> =
        context.settingsDataStore.data.map { it[Keys.LANGUAGE] ?: "" }

    suspend fun setPomodoroMinutes(minutes: Int) {
        context.settingsDataStore.edit { it[Keys.POMODORO] = minutes.coerceIn(5, 120) }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.settingsDataStore.edit {
            it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    suspend fun setThemeMode(mode: Int) {
        context.settingsDataStore.edit { it[Keys.THEME] = mode }
    }

    suspend fun setLanguage(tag: String) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = tag }
    }
}
