package com.amazon.ivs.gridfeed.repository.models

data class GridFeedItemModel(
    val id: Int,
    val imageUrl: String,
    val videoUrl: String? = null,
    val canPlayInRow: Boolean = false,
    val isFullScreen: Boolean = false,
    val isPlaying: Boolean = false,
    val gridFeedPlayer: GridFeedPlayer? = null
) {
    val isVideo = videoUrl != null
}
