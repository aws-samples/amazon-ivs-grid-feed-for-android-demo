package com.amazon.ivs.gridfeed.repository.models

import com.amazon.ivs.gridfeed.ui.feed.ScrollDirection

data class GridScrollState(
    val firstRowId: Int,
    val lastRowId: Int,
    val scrollDirection: ScrollDirection
)
