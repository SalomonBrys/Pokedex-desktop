package net.kodein.legrand_poc

import androidx.compose.runtime.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.kodein.legrand_poc.util.AppLanguage
import net.kodein.legrand_poc.view.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Legrand POC",
        state = rememberWindowState(
            size = DpSize(1400.dp, 900.dp),
        ),
    ) {
        key(AppLanguage.lang) {
            App()
        }
    }
}
