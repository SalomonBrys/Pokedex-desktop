package net.kodein.legrand_poc.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner


class ComposeViewModelStoreOwner: ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
    fun dispose() { viewModelStore.clear() }
}

@Composable
fun rememberViewModelStoreOwner(key: Any? = null): ViewModelStoreOwner {
    val viewModelStoreOwner = remember(key) { ComposeViewModelStoreOwner() }
    DisposableEffect(viewModelStoreOwner) {
        onDispose { viewModelStoreOwner.dispose() }
    }
    return viewModelStoreOwner
}

@Composable
fun ScopedViewModels(
    key: Any? = null,
    content: @Composable () -> Unit,
){
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides rememberViewModelStoreOwner(key),
    ) {
        content()
    }
}
