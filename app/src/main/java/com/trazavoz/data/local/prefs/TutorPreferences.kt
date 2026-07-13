package com.trazavoz.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "tutor_prefs")

@Singleton
class TutorPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pinKey = stringPreferencesKey("tutor_pin")

    val tutorPinFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[pinKey] ?: "0000"
    }

    suspend fun savePin(newPin: String) {
        context.dataStore.edit { preferences ->
            preferences[pinKey] = newPin
        }
    }
}
