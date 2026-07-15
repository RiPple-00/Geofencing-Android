package com.example.geofencing.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.geofencing.di.SelectedSitePreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SelectedSiteRepositoryImpl @Inject constructor(
    @SelectedSitePreferences private val dataStore: DataStore<Preferences>
) : SelectedSiteRepository {

    override val selectedSiteId: Flow<Int> = dataStore.data.map { prefs ->
        prefs[SELECTED_SITE_ID_KEY] ?: DEFAULT_SITE_ID
    }

    override suspend fun selectSite(siteId: Int) {
        dataStore.edit { prefs ->
            prefs[SELECTED_SITE_ID_KEY] = siteId
        }
    }

    private companion object {
        val SELECTED_SITE_ID_KEY = intPreferencesKey("selected_site_id")
        const val DEFAULT_SITE_ID = 1
    }
}
