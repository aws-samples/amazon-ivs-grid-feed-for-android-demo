package com.amazon.ivs.gridfeed.common

import com.amazon.ivs.gridfeed.repository.models.GridFeedItemModel
import com.amazon.ivs.gridfeed.repository.models.GridFeedPlayer
import com.amazon.ivs.gridfeed.repository.models.GridFeedRowModel
import com.amazon.ivs.gridfeed.repository.models.GridFeedVideoCounter
import com.amazon.ivs.gridfeed.repository.models.RowType
import com.amazon.ivs.gridfeed.repository.models.canPlay
import com.amazon.ivs.gridfeed.repository.models.is3
import timber.log.Timber

fun List<GridFeedItemModel>.asGridRows(): List<GridFeedRowModel> {
    val models = mutableListOf<GridFeedItemModel>()
    val items = mutableListOf<GridFeedRowModel>()
    var rowId = 0
    var rowTypeIndex = 0
    forEach { model ->
        val rowType = RowType.getRowType(rowTypeIndex)
        val add3 = rowType.is3()
        models.add(model)
        if (models.size == if (add3) 3 else 5) {
            items.add(GridFeedRowModel(
                id = rowId,
                views = models.mapIndexed { index, item -> item.copy(canPlayInRow = rowType.canPlay(index)) },
                rowType = rowType
            ))
            models.clear()
            rowId++
            rowTypeIndex = if (rowTypeIndex == 3) 0 else rowTypeIndex + 1
        }
    }
    if (models.isNotEmpty()) {
        val rowType = RowType.getRowType(rowTypeIndex)
        items.add(GridFeedRowModel(
            id = rowId,
            views = models.mapIndexed { index, item -> item.copy(canPlayInRow = rowType.canPlay(index)) },
            rowType = rowType
        ))
    }
    return items
}

fun GridFeedItemModel.addPlayerOrCopy(
    counter: GridFeedVideoCounter,
    playerPool: MutableList<GridFeedPlayer>,
    quality: String
): GridFeedItemModel {
    if (!isVideo) return copy()
    if (!canPlayInRow) return dispose()

    val hasPlayer = gridFeedPlayer != null

    // Already has a player - increase the preloaded video count and return
    if (hasPlayer) {
        Timber.d("Already has player: $this")
        // Check if preloaded video count not exceeded
        val canPreloadVideo = counter.preloadedVideos < playerPool.size
        val canPlayVideo = counter.playingVideos < counter.maxPlayingVideos && counter.playingVideos <= counter.preloadedVideos
        counter.preloadedVideos++
        counter.playingVideos++
        return if (canPreloadVideo) {
            gridFeedPlayer!!.playOrPause(canPlayVideo)
            copy()
        } else {
            dispose()
        }
    }

    // Check if preloaded video count not exceeded
    val canPreloadVideo = counter.preloadedVideos < playerPool.size
    val canPlayVideo = counter.playingVideos < counter.maxPlayingVideos && counter.playingVideos <= counter.preloadedVideos
    // Increase the playing video count for new video
    counter.preloadedVideos++
    counter.playingVideos++

    Timber.d("Handle player for: $this, $canPreloadVideo, $canPlayVideo, ${playerPool.size}")

    return if (!canPreloadVideo) {
        // Limit exceeded - dispose
        dispose()
    } else {
        // Pick a new player from the player pool.
        // It is expected that at this point in time the player pool will contain only unloaded players.
        val gridFeedPlayer = playerPool.find { it.itemId == null }
        if (gridFeedPlayer == null) {
            Timber.d("If you see this message - you are doing something wrong: $this")
            return copy()
        }

        // Init player
        gridFeedPlayer.init()
        gridFeedPlayer.loadAndPlay(id, videoUrl!!, canPlayVideo, quality)

        Timber.d("Adding mapped player for: $this, $canPlayVideo, $quality")
        copy(gridFeedPlayer = gridFeedPlayer, isPlaying = false)
    }
}

fun GridFeedItemModel.dispose(): GridFeedItemModel {
    gridFeedPlayer?.run {
        Timber.d("Removing mapped player for: ${this@dispose}")
    }
    return copy(gridFeedPlayer = null, isPlaying = false)
}
