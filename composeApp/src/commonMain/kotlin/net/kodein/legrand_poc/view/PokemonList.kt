package net.kodein.legrand_poc.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import legrand_poc.composeapp.generated.resources.Res
import legrand_poc.composeapp.generated.resources.favourites
import legrand_poc.composeapp.generated.resources.pokedex
import legrand_poc.composeapp.generated.resources.sort
import net.kodein.legrand_poc.business.AppPreferences
import net.kodein.legrand_poc.business.PokemonDetails
import net.kodein.legrand_poc.business.PokemonRef
import net.kodein.legrand_poc.business.PokemonResourceList
import net.kodein.legrand_poc.business.httpClient
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


class PokemonListVM : ViewModel() {

    data class Model(
        val pokemons: List<IndexedValue<PokemonRef<PokemonDetails>>> = emptyList(),
        val favourites: Set<String> = emptySet(),
    )

    sealed interface Intent {
        data class ToggleFavourite(val ref: PokemonRef<PokemonDetails>) : Intent
    }

    private val mModel = MutableStateFlow(Model())
    val model = mModel.asStateFlow()

    init {
        viewModelScope.launch {
            val results = httpClient.get("https://pokeapi.co/api/v2/pokemon?limit=100000")
                .body<PokemonResourceList<PokemonRef<PokemonDetails>>>().results.withIndex()
            mModel.emit(
                mModel.value.copy(pokemons = results.toList())
            )
        }
        viewModelScope.launch {
            AppPreferences.datastore.data.collect { prefs ->
                mModel.emit(
                    mModel.value.copy(favourites = prefs[AppPreferences.favouritePokemonsKey] ?: emptySet())
                )
            }
        }
    }

    private suspend fun toggleFavourite(ref: PokemonRef<PokemonDetails>) {
        AppPreferences.datastore.edit { prefs ->
            val favourites = prefs[AppPreferences.favouritePokemonsKey]?.toMutableSet() ?: mutableSetOf()
            val isFavourite = ref.name in favourites
            if (isFavourite) favourites.remove(ref.name)
            else favourites.add(ref.name)
            prefs[AppPreferences.favouritePokemonsKey] = favourites
        }
    }

    fun process(intent: Intent) {
        viewModelScope.launch {
            when (intent) {
                is Intent.ToggleFavourite -> toggleFavourite(intent.ref)
            }
        }
    }
}

@Composable
fun PokemonList(
    selectedPokemon: PokemonRef<PokemonDetails>?,
    onSelectPokemon: (PokemonRef<PokemonDetails>) -> Unit,
) {
    val vm = viewModel { PokemonListVM() }
    val model by vm.model.collectAsState()

    PokemonList(
        model = model,
        process = vm::process,
        selectedPokemon = selectedPokemon,
        onSelectPokemon = onSelectPokemon,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonList(
    model: PokemonListVM.Model,
    process: (PokemonListVM.Intent) -> Unit,
    selectedPokemon: PokemonRef<PokemonDetails>?,
    onSelectPokemon: (PokemonRef<PokemonDetails>) -> Unit,
) {
    var sorted by remember { mutableStateOf(false) }
    val pokemons = remember(model.pokemons, sorted) {
        if (sorted) model.pokemons.sortedBy { it.value.name }
        else model.pokemons.sortedBy { it.index }
    }
    Column {
        TopAppBar(
            title = { Text(stringResource(Res.string.pokedex)) },
            actions = {
                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, stringResource(Res.string.sort))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (sorted) Icon(Icons.Filled.Check, null)
                                    else Spacer(Modifier.width(24.dp))
                                    Text("Par nom", Modifier.padding(start = 8.dp))
                                }
                            },
                            onClick = { sorted = true ; menuExpanded = false },
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (!sorted) Icon(Icons.Filled.Check, null)
                                    else Spacer(Modifier.width(24.dp))
                                    Text("Par id", Modifier.padding(start = 8.dp))
                                }
                            },
                            onClick = { sorted = false ; menuExpanded = false },
                        )
                    }
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (pokemons.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            } else {
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pokemonList")
                ) {
                    items(pokemons) { (index, pokemon) ->
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onSelectPokemon(pokemon) }
                                    .background(
                                        if (pokemon == selectedPokemon) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .padding(start = 4.dp, end = 16.dp)
                            ) {
                                IconButton(
                                    onClick = { process(PokemonListVM.Intent.ToggleFavourite(pokemon)) },
                                ) {
                                    Icon(
                                        imageVector =
                                            if (pokemon.name in model.favourites) Icons.Filled.Favorite
                                            else Icons.Filled.FavoriteBorder,
                                        contentDescription = stringResource(Res.string.favourites),
                                    )
                                }
                                Text(
                                    text = pokemon.name.replaceFirstChar { it.titlecase() },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pokemonName")
                                )
                                Text(
                                    text = index.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(lazyListState),
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                )
            }
        }
    }
}


@Composable
@Preview(name = "Loading")
private fun PokemonListLoadingPreview() {
    Surface(Modifier.fillMaxSize()) {
        PokemonList(
            model = PokemonListVM.Model(),
            process = {},
            selectedPokemon = null,
            onSelectPokemon = {},
        )
    }
}

@Composable
@Preview(name = "List")
private fun PokemonListPreview() {
    Surface(Modifier.fillMaxSize()) {
        PokemonList(
            model = PokemonListVM.Model(
                pokemons = listOf(
                    IndexedValue(1, PokemonRef("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")),
                    IndexedValue(4, PokemonRef("charmander", "https://pokeapi.co/api/v2/pokemon/4/")),
                    IndexedValue(2, PokemonRef("pikachu", "https://pokeapi.co/api/v2/pokemon/25/")),
                    IndexedValue(7, PokemonRef("squirtle", "https://pokeapi.co/api/v2/pokemon/7/")),
                ),
            ),
            process = {},
            selectedPokemon = PokemonRef("squirtle", "https://pokeapi.co/api/v2/pokemon/7/"),
            onSelectPokemon = {},
        )
    }
}
