package com.amazon.ivs.gridfeed.repository.models

data class GridFeedRowModel(
    val id: Int,
    val views: List<GridFeedItemModel>,
    val rowType: RowType,
)

enum class RowType(val value: Int) {
    RIGHT_5(0),
    LEFT_5(1),
    RIGHT_3(2),
    LEFT_3(3);

    companion object {
        fun getRowType(value: Int) = values().find { it.value == value } ?: RIGHT_5
    }
}

fun RowType.is3() = this == RowType.RIGHT_3 || this == RowType.LEFT_3

fun RowType.canPlay(index: Int): Boolean {
    val canPlay = when (index) {
        0 -> this == RowType.LEFT_3 || this == RowType.LEFT_5
        1 -> this == RowType.RIGHT_3
        4 -> this == RowType.RIGHT_5
        else -> false
    }
    return canPlay
}
