package com.amazon.ivs.gridfeed.repository

import android.content.Context
import androidx.datastore.core.DataStore
import com.amazon.ivs.gridfeed.common.LOAD_MORE_DELTA
import com.amazon.ivs.gridfeed.common.addPlayerOrCopy
import com.amazon.ivs.gridfeed.common.asGridRows
import com.amazon.ivs.gridfeed.common.demoItems
import com.amazon.ivs.gridfeed.common.dispose
import com.amazon.ivs.gridfeed.common.launchMain
import com.amazon.ivs.gridfeed.common.loadMore
import com.amazon.ivs.gridfeed.repository.models.AUTO_QUALITY
import com.amazon.ivs.gridfeed.repository.models.GridFeedItemModel
import com.amazon.ivs.gridfeed.repository.models.GridFeedPlayer
import com.amazon.ivs.gridfeed.repository.models.GridFeedSettings
import com.amazon.ivs.gridfeed.ui.feed.ScrollDirection
import com.amazon.ivs.gridfeed.repository.models.GridFeedRowModel
import com.amazon.ivs.gridfeed.repository.models.GridFeedVideoCounter
import com.amazon.ivs.gridfeed.repository.models.GridScrollState
import com.amazonaws.ivs.player.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface GridFeedRepository {
    val feed: StateFlow<List<GridFeedRowModel>>
    fun setFullScreenItem(id: Int)
    fun removeFullScreenItem()
    fun onFeedScrolled(scrollState: GridScrollState)
    fun maybeLoadMore(rowId: Int)
    fun reloadFeed()
}

@Singleton
class GridFeedRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsStore: DataStore<GridFeedSettings>
) : GridFeedRepository {
    private val initialFeedItems = demoItems.loadMore().mapIndexed { index, item ->
        item.copy(id = index)
    }
    private var currentFeedItems = initialFeedItems.map { it.copy() }
    private val playerPool = mutableListOf<GridFeedPlayer>()

    private val _feed = MutableStateFlow<List<GridFeedRowModel>>(emptyList())
    override val feed = _feed.asStateFlow()

    private var maxPreloadedVideos = 1f
    private var maxPlayingVideos = 1f
    private var thumbnailQuality = AUTO_QUALITY
    private var fullscreenQuality = AUTO_QUALITY

    init {
        launchMain {
            appSettingsStore.data.collectLatest { settings ->
                updateSettings(settings) {
                    playerPool.forEach { it.dispose() }
                    playerPool.clear()
                    playerPool.addAll((0 until maxPreloadedVideos.toInt()).map { createPlayer() })
                }
            }
        }
        launchMain {
            appSettingsStore.data.first().let { settings ->
                updateSettings(settings)
            }
            reloadFeed(initialFeedItems)
        }
    }

    override fun onFeedScrolled(scrollState: GridScrollState) {
        val items = currentFeedItems
        // Find items to load
        val firstRowId = scrollState.firstRowId
        val lastRowId = scrollState.lastRowId
        val scrollDirection = scrollState.scrollDirection
        val rows = _feed.value
        val firstItemId = rows[firstRowId].views.first().id
        val lastItemId = rows[lastRowId].views.last().id
        var playingPlayers = 0
        var preloadedPlayers = 0
        Timber.d("On feed scrolled: ROW: $firstRowId, $lastRowId ITEM: $firstItemId, $lastItemId DIRECTION: $scrollDirection, SIZE: ${rows.size}")

        // Map play state to player
        fun mapPlayer(id: Int): Pair<Int, Boolean>? {
            val canPlay = playingPlayers < maxPlayingVideos
            val canPreload = preloadedPlayers < maxPreloadedVideos
            playingPlayers++
            preloadedPlayers++
            return if (canPreload) id to canPlay else null
        }

        // Find preload-able visible items
        val itemsToLoad = items.subList(firstItemId, lastItemId + 1).mapNotNull {
            if (it.canPlayInRow) mapPlayer(it.id) else null
        }.toMutableList()

        // Check if more items can be loaded
        fun loadMore() = playerPool.size - itemsToLoad.size > 0

        // Find preload-able items above the visible items
        fun preloadUp() {
            if (firstItemId - 1 <= 0) return
            (firstItemId - 1 downTo 0).forEach { id ->
                if (items[id].canPlayInRow) {
                    mapPlayer(id)?.run { itemsToLoad.add(this) }
                }
            }
        }

        // Find preload-able items bellow the visible items
        fun preloadDown() {
            if (lastItemId + 1 >= items.size) return
            (lastItemId + 1 until items.size).forEach { id ->
                if (items[id].canPlayInRow) {
                    mapPlayer(id)?.run { itemsToLoad.add(this) }
                }
            }
        }

        // Find items to preload
        if (loadMore()) {
            if (scrollDirection == ScrollDirection.UP) {
                // Preload up
                preloadUp()
                if (loadMore()) preloadDown()
            } else {
                // Preload down
                preloadDown()
                if (loadMore()) preloadUp()
            }
        }
        Timber.d("Items to preload found: $itemsToLoad")

        // Clean player pool
        playerPool.forEach { player ->
            val id = player.itemId
            if (id != null && !itemsToLoad.any { it.first == id }) {
                player.dispose()
            }
        }
        // Preload items
        val mappedItems = items.map { item ->
            itemsToLoad.find { it.first == item.id }?.let { preloadItem ->
                val isAlreadyPlaying = playerPool.any { it.itemId != null && it.itemId == preloadItem.first }
                if (isAlreadyPlaying) {
                    if (item.gridFeedPlayer == null) {
                        val player = playerPool.find { it.itemId == item.id }?.apply {
                            playOrPause(preloadItem.second)
                        }
                        item.copy(gridFeedPlayer = player)
                    } else {
                        item.gridFeedPlayer.playOrPause(preloadItem.second)
                        item.copy()
                    }
                } else {
                    // Pick a new player from the player pool.
                    // It is expected that at this point in time the player pool will contain only unloaded players.
                    val gridFeedPlayer = playerPool.find { it.itemId == null } ?: return@let item.copy()

                    // Init player
                    gridFeedPlayer.init()
                    gridFeedPlayer.loadAndPlay(item.id, item.videoUrl!!, preloadItem.second, thumbnailQuality)

                    Timber.d("Adding mapped player for: $item, $preloadItem")
                    item.copy(gridFeedPlayer = gridFeedPlayer, isPlaying = false)
                }
            } ?: item.dispose()
        }
        currentFeedItems = mappedItems

        // Return items to UI
        Timber.d("Scroll handled")
        _feed.update { mappedItems.asGridRows() }
    }

    override fun setFullScreenItem(id: Int) {
        Timber.d("Set fullscreen item: $id")
        currentFeedItems = currentFeedItems.map { item ->
            if (item.id == id) {
                if (item.gridFeedPlayer == null) {
                    val player = createPlayer()
                    player.loadAndPlayForDisposal(item.id, item.videoUrl, fullscreenQuality)
                    item.copy(isFullScreen = true, gridFeedPlayer = player)
                } else if (item.isPlaying) {
                    item.copy(isFullScreen = true)
                } else {
                    item.gridFeedPlayer.shouldDispose = true
                    item.gridFeedPlayer.playOrPause(true)
                    item.copy(isFullScreen = true)
                }
            } else {
                item.gridFeedPlayer?.setSurface(null)
                item.copy()
            }
        }
        _feed.update { currentFeedItems.asGridRows() }
    }

    override fun removeFullScreenItem() {
        currentFeedItems = currentFeedItems.map { item ->
            if (item.isFullScreen) {
                Timber.d("Remove fullscreen item and maybe dispose: ${item.gridFeedPlayer?.shouldDispose}")
                var isPlaying = item.isPlaying
                val player = if (item.gridFeedPlayer?.shouldDispose == true) {
                    isPlaying = false
                    item.gridFeedPlayer.dispose()
                    null
                } else item.gridFeedPlayer
                item.copy(isFullScreen = false, gridFeedPlayer = player, isPlaying = isPlaying)
            } else {
                item.copy()
            }
        }
        _feed.update { currentFeedItems.asGridRows() }
    }

    override fun reloadFeed() {
        Timber.d("Reloading feed")
        currentFeedItems.forEach { it.dispose() }
        reloadFeed(initialFeedItems.map { it.copy(isFullScreen = false) })
    }

    override fun maybeLoadMore(rowId: Int) {
        val lastRowId = _feed.value.last().id
        val delta = lastRowId - rowId
        // Load more items if we are close to the last row in list
        if (delta < LOAD_MORE_DELTA) {
            Timber.d("Load more: $rowId, $lastRowId, $delta")
            reloadFeed(currentFeedItems.loadMore())
        }
    }

    private fun updateSettings(settings: GridFeedSettings, onUpdated: () -> Unit = {}) {
        val updated = maxPreloadedVideos != settings.preloadedVideoCount ||
                maxPlayingVideos != settings.playingVideoCount ||
                thumbnailQuality != settings.thumbnailQuality ||
                fullscreenQuality != settings.fullscreenQuality
        maxPreloadedVideos = settings.preloadedVideoCount
        maxPlayingVideos = settings.playingVideoCount
        thumbnailQuality = settings.thumbnailQuality
        fullscreenQuality = settings.fullscreenQuality
        if (updated) {
            onUpdated()
        }
    }

    private fun reloadFeed(items: List<GridFeedItemModel>) {
        // Ensure proper IDs are given to the items
        val indexedItems = items.mapIndexed { index, item ->
            item.copy(id = index)
        }.asGridRows().flatMap { it.views }

        // Add players
        val counter = GridFeedVideoCounter(
            maxPreloadedVideos = maxPreloadedVideos,
            maxPlayingVideos = maxPlayingVideos
        )
        val mappedItems = indexedItems.map { item ->
            item.addPlayerOrCopy(
                counter = counter,
                playerPool = playerPool,
                quality = thumbnailQuality
            )
        }
        currentFeedItems = mappedItems

        // Return rows to UI
        _feed.update { mappedItems.asGridRows() }
    }

    private fun createPlayer(): GridFeedPlayer {
        val player = MediaPlayer(context)
        val gridFeedPlayer = GridFeedPlayer(player)
        gridFeedPlayer.init()
        gridFeedPlayer.onPlaying = { id, isPlaying ->
            Timber.d("Player playing: $id, $isPlaying")
            currentFeedItems = currentFeedItems.map { item ->
                if (item.id == id && item.gridFeedPlayer != null) {
                    item.copy(isPlaying = isPlaying)
                } else {
                    item.copy()
                }
            }
            _feed.update { currentFeedItems.asGridRows() }
        }
        return gridFeedPlayer
    }
}
