package net.kodein.legrand_poc.business

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringSetPreferencesKey
import okio.Path.Companion.toPath
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

object AppPreferences {
    val datastore = PreferenceDataStoreFactory.createWithPath {
        "${dataStoreDir()}/app.preferences_pb".toPath()
    }

    val favouritePokemonsKey = stringSetPreferencesKey("favourite_pokemons")
}

private fun dataStoreDir(): String {
    val appDirName = "legrand_pokedex"
    val os = System.getProperty("os.name")
    val dataDir = when {
        os.startsWith("Mac OS X") -> Path(System.getProperty("user.home"), "Library", "Application Support", appDirName)
        os.startsWith("Windows") ->
            System.getenv("LOCALAPPDATA")?.let { Path(it, appDirName) }
                ?: System.getenv("APPDATA")?.let { Path(it, appDirName) }
                ?: Path(System.getProperty("user.home"), "AppData", "Local", appDirName)
        else -> Path(System.getProperty("user.home"), ".local", "share", appDirName)
    }
    dataDir.createDirectories()
    return dataDir.toAbsolutePath().toString()
}