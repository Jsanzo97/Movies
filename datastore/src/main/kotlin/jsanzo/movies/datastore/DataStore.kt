package jsanzo.movies.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import jsanzo.movies.data.datastore.DataStoreStorage
import jsanzo.movies.data.model.DataLayoutModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "movies_preferences")
private val LAYOUT_MODE_KEY = stringPreferencesKey("layout_mode")

class DataStore(private val context: Context) : DataStoreStorage {

    override fun getLayoutMode(): Flow<DataLayoutModePreference> = context.dataStore.data.map { preferences ->
        when (preferences[LAYOUT_MODE_KEY]) {
            DataLayoutModePreference.Grid3.name -> DataLayoutModePreference.Grid3
            DataLayoutModePreference.Grid4.name -> DataLayoutModePreference.Grid4
            else -> DataLayoutModePreference.Grid2
        }
    }

    override suspend fun saveLayoutMode(mode: DataLayoutModePreference) {
        context.dataStore.edit { preferences ->
            preferences[LAYOUT_MODE_KEY] = mode.name
        }
    }
}
