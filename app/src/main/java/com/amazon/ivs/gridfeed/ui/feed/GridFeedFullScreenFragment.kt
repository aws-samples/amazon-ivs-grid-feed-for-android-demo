package com.amazon.ivs.gridfeed.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.amazon.ivs.gridfeed.R
import com.amazon.ivs.gridfeed.common.collectLatestWithLifecycle
import com.amazon.ivs.gridfeed.common.fadeAlpha
import com.amazon.ivs.gridfeed.common.loadImage
import com.amazon.ivs.gridfeed.common.loadVideoSurface
import com.amazon.ivs.gridfeed.common.setVisibleOr
import com.amazon.ivs.gridfeed.common.viewBinding
import com.amazon.ivs.gridfeed.databinding.FragmentFullScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class GridFeedFullScreenFragment : Fragment(R.layout.fragment_full_screen) {
    private val binding by viewBinding(FragmentFullScreenBinding::bind)
    private val viewModel by activityViewModels<GridFeedViewModel>()
    private var visibilityChanged = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestWithLifecycle(viewModel.feed) { feed ->
            with(binding) {
                val item = feed.first { row -> row.views.any { it.isFullScreen } }.views.first { it.isFullScreen }
                Timber.d("Full screen item loaded: $item")
                image.loadImage(item)
                if (!visibilityChanged) {
                    image.setVisibleOr(!item.isPlaying)
                    visibilityChanged = true
                } else {
                    image.fadeAlpha(!item.isPlaying)
                }
                video.loadVideoSurface(item)
            }
        }

        binding.backButton.setOnClickListener {
            viewModel.onBackClicked()
            findNavController().navigateUp()
        }
    }
}
