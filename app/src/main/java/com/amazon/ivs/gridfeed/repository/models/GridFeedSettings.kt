package com.amazon.ivs.gridfeed.repository.models

import kotlinx.serialization.Serializable

const val AUTO_QUALITY = "Auto"

@Serializable
data class GridFeedSettings(
    val preloadedVideoCount: Float = 4f,
    val playingVideoCount: Float = 3f,
    val thumbnailQuality: String = AUTO_QUALITY,
    val fullscreenQuality: String = AUTO_QUALITY,
    val logsEnabled: Boolean = false
)
