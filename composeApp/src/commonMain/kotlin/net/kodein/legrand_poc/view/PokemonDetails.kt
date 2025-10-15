package net.kodein.legrand_poc.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import legrand_poc.composeapp.generated.resources.Res
import legrand_poc.composeapp.generated.resources.description
import legrand_poc.composeapp.generated.resources.favourites
import legrand_poc.composeapp.generated.resources.language
import legrand_poc.composeapp.generated.resources.move
import legrand_poc.composeapp.generated.resources.pleaseSelectPokemon
import net.kodein.legrand_poc.business.*
import net.kodein.legrand_poc.util.AppLanguage
import net.kodein.legrand_poc.view.comp.DetailsBox
import net.kodein.legrand_poc.view.comp.TreeLeaf
import net.kodein.legrand_poc.view.comp.TreeNode
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource


class PokemonDetailsVM(
    val ref: PokemonRef<PokemonDetails>,
) : ViewModel() {

    data class Model(
        val details: PokemonDetails,
        val isFavourite: Boolean,
        val species: PokemonSpecies,
        val versions: Map<PokemonRef<PokemonVersion>, PokemonVersion> = emptyMap(),
        val moves: List<PokemonMove> = emptyList(),
        val generations: Map<PokemonRef<PokemonVersionGroup>, PokemonGeneration> = emptyMap()
    )

    sealed interface Intent {
        data object ToggleFavourite : Intent
    }

    private val mModel: MutableStateFlow<Model?> = MutableStateFlow(null)
    val model = mModel.asStateFlow()

    init {
        viewModelScope.launch {
            val details: PokemonDetails = httpClient.getFromPokemonRef(ref)
            val species: PokemonSpecies = httpClient.getFromPokemonRef(details.species)

            mModel.emit(
                Model(
                    details = details,
                    isFavourite = AppPreferences.datastore.data.first().get(AppPreferences.favouritePokemonsKey)?.contains(ref.name) ?: false,
                    species = species,
                )
            )

            launch {
                val versions = species.flavorTextEntries
                    .map { it.version }
                    .associateWith { async { PokemonCache.get(it) } }
                    .mapValues { it.value.await() }
                mModel.emit(mModel.value!!.copy(versions = versions))
            }

            launch {
                val moves = details.moves
                    .map { async { httpClient.getFromPokemonRef<PokemonMove>(it.move) } }
                    .awaitAll()
                mModel.emit(mModel.value!!.copy(moves = moves))
                val generations = moves
                    .flatMap { it.flavorTextEntries }
                    .map { it.versionGroup }
                    .distinct()
                    .associateWith { async { PokemonCache.get(PokemonCache.get(it).generation) } }
                    .mapValues { it.value.await() }
                mModel.emit(mModel.value!!.copy(generations = generations))
            }

            launch {
                AppPreferences.datastore.data.collect { prefs ->
                    mModel.emit(mModel.value!!.copy(isFavourite = prefs[AppPreferences.favouritePokemonsKey]?.contains(ref.name) ?: false))
                }
            }
        }
    }

    private suspend fun toggleFavourite() {
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
                Intent.ToggleFavourite -> toggleFavourite()
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetails(
    ref: PokemonRef<PokemonDetails>?,
) {
    Box {
        val scrollState = rememberScrollState()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TopAppBar(
                title = { Text(ref?.name?.replaceFirstChar { it.titlecase() } ?: "") },
                actions = {
                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { menuExpanded = true }
                        ) {
                            Icon(Icons.Default.Language, stringResource(Res.string.language))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            AppLanguage.availableLanguages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (AppLanguage.lang == code) Icon(Icons.Filled.Check, null)
                                            else Spacer(Modifier.width(24.dp))
                                            Text(name, Modifier.padding(start = 8.dp))
                                        }
                                    },
                                    onClick = { AppLanguage.set(code) }
                                )
                            }
                        }
                    }
                }
            )
            if (ref == null) {
                Text(
                    text = stringResource(Res.string.pleaseSelectPokemon),
                    modifier = Modifier
                        .padding(16.dp)
                )
            } else {
                val vm = viewModel { PokemonDetailsVM(ref) }
                val model by vm.model.collectAsState()
                if (model == null) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                    )
                } else {
                    PokemonDetails(
                        model = model!!,
                        process = vm::process
                    )
                }
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun PokemonDetails(
    model: PokemonDetailsVM.Model,
    process: (PokemonDetailsVM.Intent) -> Unit,
) {
    val name = model.species.names.filterLang().firstOrNull()?.name
        ?: model.details.name.replaceFirstChar { it.titlecase() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { process(PokemonDetailsVM.Intent.ToggleFavourite) },
        ) {
            Icon(
                imageVector =
                    if (model.isFavourite) Icons.Filled.Favorite
                    else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(Res.string.favourites),
            )
        }
        Text(
            text = "${model.details.id}. $name",
            style = MaterialTheme.typography.headlineLarge,
        )
    }
    AsyncImage(
        model = model.details.sprites.other?.officialArtwork?.frontDefault,
        contentDescription = null,
        modifier = Modifier
            .size(256.dp)
    )

    var descriptionExpanded by remember { mutableStateOf(true) }
    DetailsBox(
        expanded = descriptionExpanded,
        onToggleExpanded = { descriptionExpanded = !descriptionExpanded },
        title = { Text(pluralStringResource(Res.plurals.description, model.species.flavorTextEntries.size)) },
        modifier = Modifier
            .widthIn(max = 768.dp)
            .fillMaxWidth(),
    ) {
        Column {
            model.species.flavorTextEntries
                .filterLang()
                .forEach { flavorTextEntry ->
                    var textExpanded by remember { mutableStateOf(false) }
                    TreeNode(
                        expanded = textExpanded,
                        onToggleExpanded = { textExpanded = !textExpanded },
                        title = {
                            Text(
                                text = model.versions[flavorTextEntry.version]?.names?.filterLang()?.firstOrNull()?.name ?: flavorTextEntry.version.name,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        TreeLeaf {
                            Text(
                                text = flavorTextEntry.flavorText,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
        }
    }

    var movesExpanded by remember { mutableStateOf(false) }
    DetailsBox(
        expanded = movesExpanded,
        onToggleExpanded = { movesExpanded = !movesExpanded },
        title = { Text(pluralStringResource(Res.plurals.move, model.moves.size)) },
        modifier = Modifier
            .widthIn(max = 768.dp)
            .fillMaxWidth(),
    ) {
        Column {
            model.moves.forEach { move ->
                var moveExpanded by remember { mutableStateOf(false) }
                TreeNode(
                    expanded = moveExpanded,
                    onToggleExpanded = { moveExpanded = !moveExpanded },
                    title = {
                        Text(
                            text = move.names.filterLang().firstOrNull()?.name
                                ?: move.name.replaceFirstChar { it.titlecase() },
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    val flavorTexts = move.flavorTextEntries
                        .filterLang()
                        .map {
                            val generationName = model.generations[it.versionGroup]?.names?.filterLang()?.firstOrNull()?.name
                                ?: it.versionGroup.name.replaceFirstChar { it.titlecase() }
                            generationName to it.flavorText.trim()
                        }
                        .distinct()
                    flavorTexts.forEach { (generation, text) ->
                        var textExpanded by remember { mutableStateOf(true) }
                        TreeNode(
                            expanded = textExpanded,
                            onToggleExpanded = { textExpanded = !textExpanded },
                            title = {
                                Text(
                                    text = generation,
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                        ) {
                            TreeLeaf {
                                Text(text)
                            }
                        }
                    }
                }
            }
        }
    }
}

