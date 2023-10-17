package com.amazon.ivs.gridfeed.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e -> Timber.w(e, "Coroutine failed: ${e.localizedMessage}") },
    block = block
)

fun launchIO(block: suspend CoroutineScope.() -> Unit) = ioScope.launch(
    context = CoroutineExceptionHandler { _, e -> Timber.w(e, "Coroutine failed: ${e.localizedMessage}") },
    block = block
)

fun Fragment.launchUI(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend CoroutineScope.() -> Unit
) = viewLifecycleOwner.lifecycleScope.launch(
    context = CoroutineExceptionHandler { _, e ->
        Timber.e(e, "Coroutine failed: ${e.localizedMessage}")
    }
) {
    repeatOnLifecycle(state = lifecycleState, block = block)
}

fun ViewModel.launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(
    context = CoroutineExceptionHandler { _, e ->
        Timber.e(e, "Coroutine failed: ${e.localizedMessage}")
    },
    block = block
)

fun <T> Fragment.collectLatestWithLifecycle(
    flow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    collectLatest: suspend (T) -> Unit
) {
    launchUI(lifecycleState) {
        flow.collectLatest(collectLatest)
    }
}

fun <T> Flow<T>.asStateFlow(
    coroutineScope: CoroutineScope,
    initialValue: T,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000),
) = stateIn(coroutineScope, sharingStarted, initialValue)
