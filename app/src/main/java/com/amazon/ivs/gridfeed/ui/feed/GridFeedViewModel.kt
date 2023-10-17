package com.amazon.ivs.gridfeed.ui.feed

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazon.ivs.gridfeed.common.asStateFlow
import com.amazon.ivs.gridfeed.common.launch
import com.amazon.ivs.gridfeed.common.launchIO
import com.amazon.ivs.gridfeed.repository.GridFeedRepository
import com.amazon.ivs.gridfeed.repository.models.GridFeedSettings
import com.amazon.ivs.gridfeed.repository.models.GridScrollState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import javax.inject.Inject

private const val GRID_LOAD_DELAY = 200L

enum class ScrollDirection { UP, DOWN }

@HiltViewModel
class GridFeedViewModel @Inject constructor(
    private val repository: GridFeedRepository,
    private val appSettingsStore: DataStore<GridFeedSettings>
) : ViewModel() {
    private var settingsUpdated = false
    private val _onGoFullScreen = Channel<Unit>()
    private val _onSettingsUpdated = Channel<Unit>()
    private var lastScrolledRow = 0
    private var scrollDirection = ScrollDirection.DOWN

    val feed = repository.feed
    val onGoFullScreen = _onGoFullScreen.receiveAsFlow()
    val settings = appSettingsStore.data.asStateFlow(viewModelScope, GridFeedSettings())
    val onSettingsUpdated = _onSettingsUpdated.receiveAsFlow()

    fun onFeedScrolled(firstRowId: Int, lastRowId: Int) = launchIO {
        if (lastScrolledRow == firstRowId) return@launchIO
        scrollDirection = if (firstRowId > lastScrolledRow) ScrollDirection.DOWN else ScrollDirection.UP
        lastScrolledRow = firstRowId

        repository.maybeLoadMore(lastRowId)
        repository.onFeedScrolled(GridScrollState(
            firstRowId = firstRowId,
            lastRowId = lastRowId,
            scrollDirection = scrollDirection
        ))
    }

    fun onItemClicked(id: Int) = launch {
        repository.setFullScreenItem(id)
        _onGoFullScreen.send(Unit)
    }

    fun onBackClicked() = launch {
        delay(GRID_LOAD_DELAY)
        repository.removeFullScreenItem()
    }

    fun onPreloadedVideoCountChanged(count: Float) = launch {
        if (settings.value.preloadedVideoCount == count) return@launch
        Timber.d("On preloaded video count changed: $count")
        appSettingsStore.updateData { it.copy(preloadedVideoCount = count) }
        settingsUpdated = true
    }

    fun onPlayingVideoCountChanged(count: Float) = launch {
        if (settings.value.playingVideoCount == count) return@launch
        Timber.d("On playing video count changed: $count")
        appSettingsStore.updateData { it.copy(playingVideoCount = count) }
        settingsUpdated = true
    }

    fun onSettingsClosed() {
        Timber.d("Settings closed: $settingsUpdated")
        if (settingsUpdated) {
            settingsUpdated = false
            _onSettingsUpdated.trySend(Unit)
        }
    }

    fun reloadFeed() = launch {
        lastScrolledRow = 0
        repository.reloadFeed()
    }

    fun onThumbnailQualityChanged(value: String) = launch {
        if (settings.value.thumbnailQuality == value) return@launch
        appSettingsStore.updateData { it.copy(thumbnailQuality = value) }
        settingsUpdated = true
    }

    fun onFullscreenQualityChanged(value: String) = launch {
        if (settings.value.fullscreenQuality == value) return@launch
        appSettingsStore.updateData { it.copy(fullscreenQuality = value) }
        settingsUpdated = true
    }

    fun onLogsEnabled(enabled: Boolean) = launch {
        if (settings.value.logsEnabled == enabled) return@launch
        appSettingsStore.updateData { it.copy(logsEnabled = enabled) }
    }
}
