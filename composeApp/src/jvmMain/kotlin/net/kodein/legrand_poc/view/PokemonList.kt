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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import legrand_poc.composeapp.generated.resources.Res
import legrand_poc.composeapp.generated.resources.pokedex
import legrand_poc.composeapp.generated.resources.sort
import net.kodein.legrand_poc.business.PokemonDetails
import net.kodein.legrand_poc.business.PokemonRef
import net.kodein.legrand_poc.business.PokemonResourceList
import net.kodein.legrand_poc.business.httpClient
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


class PokemonListViewModel : ViewModel() {

    private val mRefs = MutableStateFlow<List<PokemonRef<PokemonDetails>>?>(null)
    val refs = mRefs.asStateFlow()

    private val mSorted = MutableStateFlow(false)
    val sorted = mSorted.asStateFlow()

    init {
        viewModelScope.launch {
            load(sorted = mSorted.value)
        }
    }

    private suspend fun load(sorted: Boolean) {
        val result: PokemonResourceList<PokemonRef<PokemonDetails>> = httpClient.get("https://pokeapi.co/api/v2/pokemon?limit=100000").body()
        mRefs.emit(
            if (sorted) result.results.sortedBy { it.name.lowercase() }
            else result.results
        )
    }

    fun sort(sorted: Boolean) {
        mSorted.value = sorted
        viewModelScope.launch {
            load(sorted = sorted)
        }
    }
}

@Composable
fun PokemonList(
    selectedPokemon: PokemonRef<PokemonDetails>?,
    onSelectPokemon: (PokemonRef<PokemonDetails>) -> Unit,
) {
    val vm = viewModel { PokemonListViewModel() }
    val refs by vm.refs.collectAsState()
    val sorted by vm.sorted.collectAsState()

    PokemonList(
        list = refs,
        selectedPokemon = selectedPokemon,
        onSelectPokemon = onSelectPokemon,
        sorted = sorted,
        sort = vm::sort,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonList(
    list: List<PokemonRef<PokemonDetails>>?,
    selectedPokemon: PokemonRef<PokemonDetails>?,
    onSelectPokemon: (PokemonRef<PokemonDetails>) -> Unit,
    sorted: Boolean,
    sort: (Boolean) -> Unit,
) {
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
                            onClick = { sort(true) ; menuExpanded = false },
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
                            onClick = { sort(false) ; menuExpanded = false },
                        )
                    }
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (list == null) {
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
                ) {
                    items(list) { pokemon ->
                        Column {
                            Text(
                                text = pokemon.name.replaceFirstChar { it.titlecase() },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .clickable { onSelectPokemon(pokemon) }
                                    .background(
                                        if (pokemon == selectedPokemon) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .fillMaxWidth()
                            )
                            HorizontalDivider()
                        }
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(lazyListState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
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
            list = null,
            selectedPokemon = null,
            onSelectPokemon = {},
            sorted = true,
            sort = {},
        )
    }
}

@Composable
@Preview(name = "List")
private fun PokemonListPreview() {
    Surface(Modifier.fillMaxSize()) {
        PokemonList(
            list = listOf(
                PokemonRef("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonRef("charmander", "https://pokeapi.co/api/v2/pokemon/4/"),
                PokemonRef("pikachu", "https://pokeapi.co/api/v2/pokemon/25/"),
                PokemonRef("squirtle", "https://pokeapi.co/api/v2/pokemon/7/"),
            ),
            selectedPokemon = PokemonRef("squirtle", "https://pokeapi.co/api/v2/pokemon/7/"),
            onSelectPokemon = {},
            sorted = true,
            sort = {},
        )
    }
}
