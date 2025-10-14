package net.kodein.legrand_poc.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.*


object AppLanguage {

    var lang by mutableStateOf(Locale.getDefault().language)
    private set

    val availableLanguages = mapOf(
        "en" to "English",
        "fr" to "Français",
    )

    init {
        if (lang !in availableLanguages) {
            lang = "en"
        }
    }

    fun set(lang: String) {
        Locale.setDefault(Locale(lang))
        this.lang = lang
    }

}
