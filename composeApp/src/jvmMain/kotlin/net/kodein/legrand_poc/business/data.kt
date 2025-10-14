package net.kodein.legrand_poc.business

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import legrand_poc.composeapp.generated.resources.Res
import legrand_poc.composeapp.generated.resources.langCode
import org.jetbrains.compose.resources.stringResource


@Serializable
data class PokemonResourceList<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>,
)

@Serializable
data class PokemonRef<T : Any>(
    val name: String,
    val url: String,
)

@Serializable
data class PokemonDetails(
    val id: Int,
    val name: String,
    val species: PokemonRef<PokemonSpecies>,
    val sprites: Sprites,
    val moves: List<Move>,
) {
    @Serializable
    data class Sprites(
        val other: Other?,
    ) {
        @Serializable
        data class Other(
            @SerialName("official-artwork") val officialArtwork: Sprite? = null,
            val home: Sprite? = null,
        ) {
            @Serializable
            data class Sprite(
                @SerialName("front_default") val frontDefault: String,
            )
        }
    }

    @Serializable
    data class Move(
        val move: PokemonRef<PokemonMove>,
    )
}

@Serializable
data class PokemonLanguageRef(
    val name: String,
)

interface PokemonLocalized {
    val language: PokemonLanguageRef
}

@Composable
fun <T : PokemonLocalized> List<T>.filterLang(): List<T> {
    val langCode = stringResource(Res.string.langCode).takeIf { it.isNotBlank() } ?: "en"
    return filter { it.language.name == langCode }.takeIf { it.isNotEmpty() }
        ?: filter { it.language.name == "en" }
}

@Serializable
data class PokemonName(
    override val language: PokemonLanguageRef,
    val name: String,
) : PokemonLocalized

@Serializable
data class PokemonSpecies(
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
    val names: List<PokemonName>,
) {
    @Serializable
    data class FlavorTextEntry(
        @SerialName("flavor_text") val flavorText: String,
        override val language: PokemonLanguageRef,
        val version: PokemonRef<PokemonVersion>,
    ) : PokemonLocalized

}

@Serializable
data class PokemonVersion(
    val names: List<PokemonName>,
)

@Serializable
data class PokemonVersionGroup(
    val generation: PokemonRef<PokemonGeneration>,
)

@Serializable
data class PokemonGeneration(
    val names: List<PokemonName>,
)

@Serializable
data class PokemonMove(
    val name: String,
    val generation: PokemonRef<PokemonGeneration>,
    val names: List<PokemonName>,
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
) {
    @Serializable
    data class FlavorTextEntry(
        @SerialName("flavor_text") val flavorText: String,
        override val language: PokemonLanguageRef,
        @SerialName("version_group") val versionGroup: PokemonRef<PokemonVersionGroup>,
    ) : PokemonLocalized
}
