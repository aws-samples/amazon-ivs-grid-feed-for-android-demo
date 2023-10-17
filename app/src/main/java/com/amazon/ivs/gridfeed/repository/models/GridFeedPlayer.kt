package com.amazon.ivs.gridfeed.repository.models

import android.net.Uri
import android.util.Size
import android.view.Surface
import com.amazon.ivs.gridfeed.common.addListener
import com.amazonaws.ivs.player.MediaPlayer
import com.amazonaws.ivs.player.Player
import timber.log.Timber

data class GridFeedPlayer(
    private val player: MediaPlayer,
    var onPlaying: (Int, Boolean) -> Unit = { _, _ -> },
    var onSizeChanged: (Size) -> Unit = {},
    var itemId: Int? = null,
    var videoUrl: String? = null,
    var shouldDispose: Boolean = false,
    var canPlay: Boolean = false,
) {
    private var listener: Player.Listener? = null

    fun init() {
        Timber.d("Initializing player: $itemId")
        listener?.run { player.removeListener(this) }
        listener = player.addListener(
            onVideoSizeChanged = { videoSizeState ->
                if (itemId == null) return@addListener
                onSizeChanged(videoSizeState)
            },
            onStateChanged = { state ->
                val currentId = itemId ?: return@addListener
                Timber.d("Video state changed: $state for $currentId")
                onPlaying(currentId, state == Player.State.PLAYING)
            },
            onError = {
                val currentId = itemId ?: return@addListener
                Timber.d("Playback failed for: $currentId")
                onPlaying(currentId, false)
            }
        )
    }

    fun loadAndPlay(id: Int, url: String, play: Boolean, quality: String?) {
        Timber.d("Loading player: $id, $play, $quality, $itemId")
        itemId = id
        videoUrl = url
        player.load(Uri.parse(url))
        if (quality != null) {
            player.qualities.firstOrNull { it.name == quality }?.let { foundQuality ->
                player.setAutoQualityMode(false, false)
                player.setAutoMaxQuality(foundQuality)
            } ?: player.setAutoQualityMode(true, true)
        } else {
            player.setAutoQualityMode(true, true)
        }
        playOrPause(play)
    }

    fun playOrPause(play: Boolean = canPlay) {
        canPlay = play
        val isPlaying = player.state == Player.State.PLAYING
        if (canPlay) {
            if (!isPlaying) {
                Timber.d("Playing video: $itemId")
                player.play()
            }
        } else {
            Timber.d("Pausing video: $itemId, $isPlaying")
            player.pause()
        }
    }

    fun dispose() {
        Timber.d("Disposing player: $itemId")
        listener?.run { player.removeListener(this) }
        listener = null
        videoUrl = null
        itemId = null
        onSizeChanged = {}
        shouldDispose = false
        player.pause()
        player.setSurface(null)
    }

    fun setSurface(surface: Surface?) {
        Timber.d("Setting surface: $itemId")
        player.setSurface(surface)
    }

    fun loadAndPlayForDisposal(id: Int, url: String?, quality: String?) {
        Timber.d("Load for disposal: $id")
        if (url == null) return
        loadAndPlay(id, url, true, quality)
        shouldDispose = true
        canPlay = true
    }
}
