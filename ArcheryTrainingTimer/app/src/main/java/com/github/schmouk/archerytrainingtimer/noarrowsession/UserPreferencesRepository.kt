/*
MIT License

Copyright (c) 2025 Philippe Schmouker, ph (dot) schmouker (at) gmail (dot) com

This file is part of Android application ArcheryTrainingTimer.

Permission is hereby granted,  free of charge,  to any person obtaining a copy
of this software and associated documentation files (the "Software"),  to deal
in the Software without restriction,  including without limitation the  rights
to use,  copy,  modify,  merge,  publish,  distribute, sublicense, and/or sell
copies of the Software,  and  to  permit  persons  to  whom  the  Software  is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS",  WITHOUT WARRANTY OF ANY  KIND,  EXPRESS  OR
IMPLIED,  INCLUDING  BUT  NOT  LIMITED  TO  THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT  SHALL  THE
AUTHORS  OR  COPYRIGHT  HOLDERS  BE  LIABLE  FOR  ANY CLAIM,  DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM,
OUT  OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.github.schmouk.archerytrainingtimer.noarrowsession

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

// Define the DataStore instance at the top level, associated with the application context
// The name "user_preferences" will be the filename for the DataStore file.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

// Define a TAG for logging
private const val USER_PREFERENCES_TAG = "UserPreferencesRepo"

data class UserPreferences(
    val selectedDuration: String?,
    val numberOfRepetitions: Int?,
    val numberOfSeries: Int?,
    val intermediateBeeps: Boolean?
)

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    // Define keys for each preference
    private object PreferencesKeys {
        val SELECTED_DURATION = stringPreferencesKey("selected_duration")
        val NUMBER_OF_REPETITIONS = intPreferencesKey("number_of_repetitions")
        val NUMBER_OF_SERIES = intPreferencesKey("number_of_series")
        val INTERMEDIATE_BEEPS = booleanPreferencesKey("intermediate_beeps")
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
            val intermediateBeepsFlag = preferences[PreferencesKeys.INTERMEDIATE_BEEPS]
            val intermediateBeeps = intermediateBeepsFlag ?: false

            UserPreferences(selectedDuration, numberOfRepetitions, numberOfSeries, intermediateBeeps)  //saveSelection)
        }


    // Function to save only the duration preference
    suspend fun saveDurationPreference(duration: String?) {
        dataStore.edit { preferences ->
            if (duration == null) preferences.remove(PreferencesKeys.SELECTED_DURATION)
            else preferences[PreferencesKeys.SELECTED_DURATION] = duration
        }
    }

    // Function to save only the repetitions preference
    suspend fun saveRepetitionsPreference(repetitions: Int?) {
        dataStore.edit { preferences ->
            if (repetitions == null) preferences.remove(PreferencesKeys.NUMBER_OF_REPETITIONS)
            else preferences[PreferencesKeys.NUMBER_OF_REPETITIONS] = repetitions
        }
    }

    // Function to save only the series preference
    suspend fun saveSeriesPreference(series: Int?) {
        dataStore.edit { preferences ->
            if (series == null) preferences.remove(PreferencesKeys.NUMBER_OF_SERIES)
            else preferences[PreferencesKeys.NUMBER_OF_SERIES] = series
        }
    }

    // Function to save only the intermediateBeeps preference
    suspend fun saveIntermediateBeepsPreference(intermediateBeeps: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INTERMEDIATE_BEEPS] = intermediateBeeps
        }
    }

}
