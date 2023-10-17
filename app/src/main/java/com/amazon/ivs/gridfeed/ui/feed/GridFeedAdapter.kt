package com.amazon.ivs.gridfeed.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amazon.ivs.gridfeed.common.fadeAlpha
import com.amazon.ivs.gridfeed.common.loadImage
import com.amazon.ivs.gridfeed.common.loadVideoSurface
import com.amazon.ivs.gridfeed.common.setVisibleOr
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowLeft3Binding
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowLeft5Binding
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowRight3Binding
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowRight5Binding
import com.amazon.ivs.gridfeed.repository.models.GridFeedRowModel
import com.amazon.ivs.gridfeed.ui.models.MergedBinding
import com.amazon.ivs.gridfeed.repository.models.RowType
import timber.log.Timber

private val adapterDiff = object : DiffUtil.ItemCallback<GridFeedRowModel>() {
    override fun areItemsTheSame(oldItem: GridFeedRowModel, newItem: GridFeedRowModel) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: GridFeedRowModel, newItem: GridFeedRowModel) =
        oldItem == newItem
}

class GridFeedAdapter(
    private val onItemClicked: (Int) -> Unit,
) : ListAdapter<GridFeedRowModel, RecyclerView.ViewHolder>(adapterDiff) {

    override fun getItemViewType(position: Int) = currentList[position].rowType.value

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            RowType.RIGHT_3.value -> {
                Right3ViewHolder(ItemFeedRowRight3Binding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            RowType.RIGHT_5.value -> {
                Right5ViewHolder(ItemFeedRowRight5Binding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            RowType.LEFT_3.value -> {
                Left3ViewHolder(ItemFeedRowLeft3Binding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            RowType.LEFT_5.value -> {
                Left5ViewHolder(ItemFeedRowLeft5Binding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else -> {
                throw Exception("You are doing something wrong buddy")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        when (holder.itemViewType) {
            RowType.RIGHT_3.value -> {
                (holder as Right3ViewHolder).bind(item)
            }
            RowType.RIGHT_5.value -> {
                (holder as Right5ViewHolder).bind(item)
            }
            RowType.LEFT_3.value -> {
                (holder as Left3ViewHolder).bind(item)
            }
            RowType.LEFT_5.value -> {
                (holder as Left5ViewHolder).bind(item)
            }
            else -> {
                throw Exception("You are doing something wrong buddy")
            }
        }
    }

    inner class Right3ViewHolder(private val binding: ItemFeedRowRight3Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GridFeedRowModel) {
            val mergedBinding = MergedBinding()
            mergedBinding.mergeRight3(binding)
            mergedBinding.bind(item)
        }
    }

    inner class Right5ViewHolder(private val binding: ItemFeedRowRight5Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GridFeedRowModel) {
            val mergedBinding = MergedBinding()
            mergedBinding.mergeRight5(binding)
            mergedBinding.bind(item)
        }
    }

    inner class Left3ViewHolder(private val binding: ItemFeedRowLeft3Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GridFeedRowModel) {
            val mergedBinding = MergedBinding()
            mergedBinding.mergeLeft3(binding)
            mergedBinding.bind(item)
        }
    }

    inner class Left5ViewHolder(private val binding: ItemFeedRowLeft5Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GridFeedRowModel) {
            val mergedBinding = MergedBinding()
            mergedBinding.mergeLeft5(binding)
            mergedBinding.bind(item)
        }
    }

    private fun MergedBinding.bind(item: GridFeedRowModel) {
        val buttons = listOf(image1Button, image2Button, image3Button, image4Button, image5Button)
        var ids = "${item.id} ["
        repeat(buttons.size) { index ->
            val currentItem = item.views.getOrNull(index)
            ids += currentItem?.id?.toString()?.plus(",") ?: ""
            if (currentItem?.isVideo == false) return@repeat

            buttons[index]?.setOnClickListener {
                currentItem?.run { onItemClicked(id) }
            }
        }
        ids = ids.dropLast(1) + "]"
        rowIndex.text = ids

        image1Item.setVisibleOr(false, View.INVISIBLE)
        image2Item.setVisibleOr(false, View.INVISIBLE)
        image3Item.setVisibleOr(false, View.INVISIBLE)
        image4Item?.setVisibleOr(false, View.INVISIBLE)
        image5Item?.setVisibleOr(false, View.INVISIBLE)
        image1Video.setVisibleOr(false)
        image2Video.setVisibleOr(false)
        image3Video.setVisibleOr(false)
        image4Video?.setVisibleOr(false)
        image5Video?.setVisibleOr(false)

        // TODO: Do we want to reset the thumbnail visibility here? Either way - fix the fade animation on resume
        image1.setVisibleOr()
        image2.setVisibleOr()
        image5?.setVisibleOr()

        image3.setVisibleOr()
        image4?.setVisibleOr()
        video1.setVisibleOr(false)
        video2.setVisibleOr(false)
        video3.setVisibleOr(false)
        video4?.setVisibleOr(false)
        video5?.setVisibleOr(false)
        item.views.forEachIndexed { index, model ->
            when (index) {
                0 -> {
                    image1Item.setVisibleOr()
                    image1Video.setVisibleOr(model.isVideo)
                    video1.setVisibleOr(model.canPlayInRow)
                    image1.loadImage(model)
                    if (model.canPlayInRow) {
                        Timber.d("Load video: ${model.id}, ${model.isPlaying}")
                        video1.loadVideoSurface(model)
                        image1.fadeAlpha(!model.isPlaying)
                    }
                }
                1 -> {
                    image2Item.setVisibleOr()
                    image2Video.setVisibleOr(model.isVideo)
                    video2.setVisibleOr(model.canPlayInRow)
                    image2.loadImage(model)
                    if (model.canPlayInRow) {
                        Timber.d("Load video: ${model.id}, ${model.isPlaying}")
                        video2.loadVideoSurface(model)
                        image2.fadeAlpha(!model.isPlaying)
                    }
                }
                2 -> {
                    image3Item.setVisibleOr()
                    image3Video.setVisibleOr(model.isVideo)
                    video3.setVisibleOr(model.canPlayInRow)
                    image3.loadImage(model)
                }
                3 -> {
                    image4Item?.setVisibleOr()
                    image4Video?.setVisibleOr(model.isVideo)
                    video4?.setVisibleOr(model.canPlayInRow)
                    image4?.loadImage(model)
                }
                4 -> {
                    image5Item?.setVisibleOr()
                    image5Video?.setVisibleOr(model.isVideo)
                    video5?.setVisibleOr(model.canPlayInRow)
                    image5?.loadImage(model)
                    if (model.canPlayInRow) {
                        Timber.d("Load video: ${model.id}, ${model.isPlaying}")
                        video5?.loadVideoSurface(model)
                        image5?.fadeAlpha(!model.isPlaying)
                    }
                }
            }
        }
    }
}
