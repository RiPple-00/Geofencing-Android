package com.example.geofencing.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ViolationAckRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViolationAckRepository {

    override val lastAcknowledgedAt: Flow<Instant> = dataStore.data.map { prefs ->
        prefs[LAST_ACKNOWLEDGED_AT_KEY]?.let(Instant::ofEpochMilli) ?: Instant.EPOCH
    }

    override suspend fun acknowledgeNow() {
        dataStore.edit { prefs ->
            prefs[LAST_ACKNOWLEDGED_AT_KEY] = Instant.now().toEpochMilli()
        }
    }

    private companion object {
        val LAST_ACKNOWLEDGED_AT_KEY = longPreferencesKey("last_acknowledged_violation_at")
    }
}
