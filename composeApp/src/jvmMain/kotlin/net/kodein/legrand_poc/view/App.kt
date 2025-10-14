package net.kodein.legrand_poc.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import net.kodein.legrand_poc.business.PokemonDetails
import net.kodein.legrand_poc.business.PokemonRef
import net.kodein.legrand_poc.util.ScopedViewModels
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import java.awt.Cursor

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            var selectedPokemon: PokemonRef<PokemonDetails>? by remember { mutableStateOf(null) }
            HorizontalSplitPane {
                first(320.dp) {
                    PokemonList(
                        selectedPokemon = selectedPokemon,
                        onSelectPokemon = { selectedPokemon = it },
                    )
                }
                second(512.dp) {
                    ScopedViewModels(selectedPokemon) {
                        PokemonDetails(selectedPokemon)
                    }
                }
                splitter {
                    visiblePart {
                        Box(
                            Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.onBackground)
                        )
                    }
                    handle {
                        Box(
                            Modifier
                                .markAsHandle()
                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                                .width(8.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

