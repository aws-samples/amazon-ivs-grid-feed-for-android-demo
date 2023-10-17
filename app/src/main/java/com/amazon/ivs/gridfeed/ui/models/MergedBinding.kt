package com.amazon.ivs.gridfeed.ui.models

import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowLeft3Binding
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowLeft5Binding
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowRight3Binding
import com.amazon.ivs.gridfeed.databinding.ItemFeedRowRight5Binding

class MergedBinding {
    lateinit var image1: ImageView
    lateinit var image1Button: View
    lateinit var image1Item: FrameLayout
    lateinit var image1Video: ImageView
    lateinit var image2: ImageView
    lateinit var image2Button: View
    lateinit var image2Item: FrameLayout
    lateinit var image2Video: ImageView
    lateinit var image3: ImageView
    lateinit var image3Button: View
    lateinit var image3Item: FrameLayout
    lateinit var image3Video: ImageView
    lateinit var video1: TextureView
    lateinit var video2: TextureView
    lateinit var video3: TextureView
    lateinit var rowIndex: TextView
    var video4: TextureView? = null
    var video5: TextureView? = null
    var image4: ImageView? = null
    var image5: ImageView? = null
    var image4Button: View? = null
    var image5Button: View? = null
    var image4Item: FrameLayout? = null
    var image5Item: FrameLayout? = null
    var image4Video: ImageView? = null
    var image5Video: ImageView? = null

    fun mergeRight3(binding: ItemFeedRowRight3Binding) {
        image1 = binding.image1
        image1Button = binding.image1Button
        image1Item = binding.image1Item
        image1Video = binding.image1Video
        image2 = binding.image2
        image2Button = binding.image2Button
        image2Item = binding.image2Item
        image2Video = binding.image2Video
        image3 = binding.image3
        image3Button = binding.image3Button
        image3Item = binding.image3Item
        image3Video = binding.image3Video
        video1 = binding.video1
        video2 = binding.video2
        video3 = binding.video3
        rowIndex = binding.rowIndex
    }

    fun mergeRight5(binding: ItemFeedRowRight5Binding) {
        image1 = binding.image1
        image1Button = binding.image1Button
        image1Item = binding.image1Item
        image1Video = binding.image1Video
        image2 = binding.image2
        image2Button = binding.image2Button
        image2Item = binding.image2Item
        image2Video = binding.image2Video
        image3 = binding.image3
        image3Button = binding.image3Button
        image3Item = binding.image3Item
        image3Video = binding.image3Video
        image4 = binding.image4
        image4Button = binding.image4Button
        image4Item = binding.image4Item
        image4Video = binding.image4Video
        image5 = binding.image5
        image5Button = binding.image5Button
        image5Item = binding.image5Item
        image5Video = binding.image5Video
        video1 = binding.video1
        video2 = binding.video2
        video3 = binding.video3
        video4 = binding.video4
        video5 = binding.video5
        rowIndex = binding.rowIndex
    }

    fun mergeLeft3(binding: ItemFeedRowLeft3Binding) {
        image1 = binding.image1
        image1Button = binding.image1Button
        image1Item = binding.image1Item
        image1Video = binding.image1Video
        image2 = binding.image2
        image2Button = binding.image2Button
        image2Item = binding.image2Item
        image2Video = binding.image2Video
        image3 = binding.image3
        image3Button = binding.image3Button
        image3Item = binding.image3Item
        image3Video = binding.image3Video
        video1 = binding.video1
        video2 = binding.video2
        video3 = binding.video3
        rowIndex = binding.rowIndex
    }

    fun mergeLeft5(binding: ItemFeedRowLeft5Binding) {
        image1 = binding.image1
        image1Button = binding.image1Button
        image1Item = binding.image1Item
        image1Video = binding.image1Video
        image2 = binding.image2
        image2Button = binding.image2Button
        image2Item = binding.image2Item
        image2Video = binding.image2Video
        image3 = binding.image3
        image3Button = binding.image3Button
        image3Item = binding.image3Item
        image3Video = binding.image3Video
        image4 = binding.image4
        image4Button = binding.image4Button
        image4Item = binding.image4Item
        image4Video = binding.image4Video
        image5 = binding.image5
        image5Button = binding.image5Button
        image5Item = binding.image5Item
        image5Video = binding.image5Video
        video1 = binding.video1
        video2 = binding.video2
        video3 = binding.video3
        video4 = binding.video4
        video5 = binding.video5
        rowIndex = binding.rowIndex
    }
}
