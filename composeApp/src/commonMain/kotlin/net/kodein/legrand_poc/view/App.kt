package net.kodein.legrand_poc.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import net.kodein.legrand_poc.business.PokemonDetails
import net.kodein.legrand_poc.business.PokemonRef
import net.kodein.legrand_poc.util.ScopedViewModels

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            val navigator = rememberListDetailPaneScaffoldNavigator<PokemonRef<PokemonDetails>>()
            val scope = rememberCoroutineScope()
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        PokemonList(
                            selectedPokemon = navigator.currentDestination?.contentKey,
                            onSelectPokemon = {
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                                }
                            }
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        val currentPokemon = navigator.currentDestination?.contentKey
                        ScopedViewModels(currentPokemon) {
                            PokemonDetails(
                                ref = currentPokemon,
                                back = if (currentPokemon != null) ({ scope.launch { navigator.navigateBack() } }) else null
                            )
                        }
                    }
                },
                paneExpansionDragHandle = { state ->
//                    val interactionSource = remember { MutableInteractionSource() }
                    VerticalDragHandle(
//                        interactionSource = interactionSource,
                        modifier = Modifier
//                            .paneExpansionDraggable(
//                                state,
//                                LocalMinimumInteractiveComponentSize.current,
//                                interactionSource
//                            ),
                    )
                }
            )
        }
    }
}

