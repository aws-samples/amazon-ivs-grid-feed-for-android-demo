package com.amazon.ivs.gridfeed.ui.feed

import androidx.lifecycle.ViewModel
import com.amazon.ivs.gridfeed.common.launch
import com.amazon.ivs.gridfeed.common.launchIO
import com.amazon.ivs.gridfeed.repository.GridFeedRepository
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
) : ViewModel() {
    private var settingsUpdated = false
    private val _onGoFullScreen = Channel<Unit>()
    private val _onSettingsUpdated = Channel<Unit>()
    private var lastScrolledRow = 0
    private var scrollDirection = ScrollDirection.DOWN

    val feed = repository.feed
    val settings = repository.settings
    val onGoFullScreen = _onGoFullScreen.receiveAsFlow()
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

    fun onPreloadedVideoCountChanged(count: Float) {
        repository.onPreloadedVideoCountChanged(
            count = count,
            onUpdated = { settingsUpdated = true }
        )
    }

    fun onPlayingVideoCountChanged(count: Float) {
        repository.onPlayingVideoCountChanged(
            count = count,
            onUpdated = { settingsUpdated = true }
        )
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

    fun onThumbnailQualityChanged(value: String) {
        repository.onThumbnailQualityChanged(
            value = value,
            onUpdated = { settingsUpdated = true }
        )
    }

    fun onFullscreenQualityChanged(value: String) {
        repository.onFullscreenQualityChanged(
            value = value,
            onUpdated = { settingsUpdated = true }
        )
    }

    fun onLogsEnabled(enabled: Boolean) {
        repository.onLogsEnabled(enabled = enabled)
    }
}
