package com.example.archerytrainingtimer

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlin.collections.remove
import kotlin.text.clear

// Define the DataStore instance at the top level, associated with the application context
// The name "user_preferences" will be the filename for the DataStore file.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Define a TAG for logging
private const val USER_PREFERENCES_TAG = "UserPreferencesRepo"

data class UserPreferences(
    val selectedDuration: String?,
    val numberOfRepetitions: Int?,
    val numberOfSeries: Int?,
    val saveSelection: Boolean
)

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    // Define keys for each preference
    // It's good practice to make these private object properties
    private object PreferencesKeys {
        val SELECTED_DURATION = stringPreferencesKey("selected_duration")
        val NUMBER_OF_REPETITIONS = intPreferencesKey("number_of_repetitions")
        val NUMBER_OF_SERIES = intPreferencesKey("number_of_series")
        val SAVE_SELECTION = booleanPreferencesKey("save_selection")
    }

    // Flow to read all user preferences
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(USER_PREFERENCES_TAG, "--Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val selectedDuration = preferences[PreferencesKeys.SELECTED_DURATION]
            val numberOfRepetitions = preferences[PreferencesKeys.NUMBER_OF_REPETITIONS]
            val numberOfSeries = preferences[PreferencesKeys.NUMBER_OF_SERIES]
            val loadedSaveFlag = preferences[PreferencesKeys.SAVE_SELECTION] // ADD THIS
            val saveSelection = loadedSaveFlag ?: false
                //preferences[PreferencesKeys.SAVE_SELECTION] ?: false // Default to false if not set

            UserPreferences(selectedDuration, numberOfRepetitions, numberOfSeries, saveSelection)
        }

    // Combined function to save all preferences if needed,
    // especially useful if "saveSelection" is true
    suspend fun saveAllPreferences(
        duration: String?,
        repetitions: Int?,
        series: Int?,
        saveSelectionFlag: Boolean
    ) {
        dataStore.edit { preferences ->
            if (duration == null) preferences.remove(PreferencesKeys.SELECTED_DURATION)
            else preferences[PreferencesKeys.SELECTED_DURATION] = duration

            if (repetitions == null) preferences.remove(PreferencesKeys.NUMBER_OF_REPETITIONS)
            else preferences[PreferencesKeys.NUMBER_OF_REPETITIONS] = repetitions

            if (series == null) preferences.remove(PreferencesKeys.NUMBER_OF_SERIES)
            else preferences[PreferencesKeys.NUMBER_OF_SERIES] = series

            preferences[PreferencesKeys.SAVE_SELECTION] = saveSelectionFlag
        }
    }

    // Function to clear preferences if the user unchecks "saveSelection"
    // and we decide not to keep the values.
    suspend fun clearAllPreferencesIfSaveIsUnchecked() {
        dataStore.edit { preferences ->
            preferences.clear() // Clears all preferences
            // Or you could selectively remove them:
            // preferences.remove(PreferencesKeys.SELECTED_DURATION)
            // preferences.remove(PreferencesKeys.NUMBER_OF_REPETITIONS)
            // preferences.remove(PreferencesKeys.NUMBER_OF_SERIES)
            // preferences[PreferencesKeys.SAVE_SELECTION] = false // Keep the save flag as false
        }
    }
}
