@file:Suppress("unused")

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.kodein.legrand_poc.view.comp.DetailsBox
import net.kodein.legrand_poc.view.comp.TreeLeaf
import net.kodein.legrand_poc.view.comp.TreeNode
import org.jetbrains.compose.storytale.story

const val loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur non elementum erat. Sed lorem" +
        "risus, blandit non arcu ac, ullamcorper congue felis. Curabitur feugiat diam nunc, ut porttitor mi bibendum sed." +
        "Vestibulum eget eros metus. Suspendisse quam enim, maximus ut semper ut, iaculis a risus. Sed pulvinar arcu in" +
        "nunc posuere, non sollicitudin lectus imperdiet. Duis finibus dui lacus, nec varius eros congue eget. Aenean" +
        "hendrerit scelerisque volutpat. Nullam id lacus sed nisl semper facilisis. Class aptent taciti sociosqu ad" +
        "litora torquent per conubia nostra, per inceptos himenaeos. Ut viverra scelerisque finibus. Nunc non libero" +
        "vitae dui sollicitudin semper. Sed ac ipsum venenatis, malesuada ipsum ut, lobortis ipsum."

val `Details Box` by story {
    var expanded by remember { mutableStateOf(false) }

    val title by parameter("Title of the box")
    val width by parameter(512)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        DetailsBox(
            title = { Text(title) },
            expanded = expanded,
            onToggleExpanded = { expanded = !expanded },
            modifier = Modifier
                .width(width.dp)
        ) {
            Text(loremIpsum)
        }
    }
}

val `Tree Nodes` by story {
    var RootExpanded by remember { mutableStateOf(false) }
    var InterExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .width(256.dp)
    ) {
        TreeNode(
            expanded = RootExpanded,
            onToggleExpanded = { RootExpanded = !RootExpanded },
            title = { Text("Root Node") },
        ) {
            TreeLeaf { Text("Leaf 1") }

            TreeNode(
                expanded = InterExpanded,
                onToggleExpanded = { InterExpanded = !InterExpanded },
                title = { Text("Inter Node") },
            ) {
                TreeLeaf { Text("Leaf 3") }
            }

            TreeLeaf { Text("Leaf 2") }
        }
    }
}
