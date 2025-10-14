package net.kodein.legrand_poc.view.comp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp


@Composable
fun TreeNode(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onToggleExpanded() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .rotate(animateFloatAsState(if (expanded) 90f else 0f).value)
            )
            Spacer(Modifier.width(8.dp))
            title()
        }
        AnimatedVisibility(
            visible = expanded,
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun TreeLeaf(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .padding(start = 16.dp)
    ) {
        content()
    }
}
