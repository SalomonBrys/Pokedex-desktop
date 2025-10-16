import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import net.kodein.legrand_poc.util.ScopedViewModels
import net.kodein.legrand_poc.view.PokemonList
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ListTests {

    @Test
    fun `First Pokemon names`() = runComposeUiTest {
        setContent {
            ScopedViewModels {
                PokemonList(
                    selectedPokemon = null,
                    onSelectPokemon = {}
                )
            }
        }
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        waitUntilDoesNotExist(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
        val nodes = onAllNodesWithTag("pokemonName", useUnmergedTree = true)
        onNodeWithTag("pokemonList").assertExists()
        nodes[0].assertTextEquals("Bulbasaur")
        nodes[1].assertTextEquals("Ivysaur")
    }

}
