package com.amazon.ivs.gridfeed.repository.models

data class GridFeedVideoCounter(
    val maxPreloadedVideos: Float,
    val maxPlayingVideos: Float,
    var preloadedVideos: Int = 0,
    var playingVideos: Int = 0
)
