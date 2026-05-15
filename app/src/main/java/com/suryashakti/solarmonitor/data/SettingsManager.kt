package com.suryashakti.solarmonitor.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    val unitRate: Flow<Double> = dataStore.data.map { it[UNIT_RATE] ?: 8.0 }
    val exportRate: Flow<Double> = dataStore.data.map { it[EXPORT_RATE] ?: 4.0 }

    suspend fun saveRates(unit: Double, export: Double) {
        dataStore.edit { prefs ->
            prefs[UNIT_RATE] = unit
            prefs[EXPORT_RATE] = export
        }
    }

    companion object {
        val UNIT_RATE = doublePreferencesKey("unit_rate")
        val EXPORT_RATE = doublePreferencesKey("export_rate")
    }
}
