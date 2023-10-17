package com.amazon.ivs.gridfeed.ui.feed

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.amazon.ivs.gridfeed.R
import com.amazon.ivs.gridfeed.common.collectLatestWithLifecycle
import com.amazon.ivs.gridfeed.common.onSelectionChanged
import com.amazon.ivs.gridfeed.common.viewBinding
import com.amazon.ivs.gridfeed.databinding.BottomSheetSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GridFeedSettingsFragment : BottomSheetDialogFragment(R.layout.bottom_sheet_settings) {
    private val binding by viewBinding(BottomSheetSettingsBinding::bind)
    private val viewModel by activityViewModels<GridFeedViewModel>()
    private val thumbnailAdapter by lazy {
        ArrayAdapter.createFromResource(requireContext(), R.array.qualities, R.layout.item_spinner)
    }
    private val fullscreenAdapter by lazy {
        ArrayAdapter.createFromResource(requireContext(), R.array.qualities, R.layout.item_spinner)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectLatestWithLifecycle(viewModel.settings) { settings ->
            with(binding) {
                val qualities = resources.getStringArray(R.array.qualities)
                preloadedVideoLabel.text = getString(R.string.preloaded_video_count, settings.preloadedVideoCount.toInt().toString())
                playingVideoLabel.text = getString(R.string.playing_video_count, settings.playingVideoCount.toInt().toString())
                preloadedVideoCount.value = settings.preloadedVideoCount
                playingVideoCount.value = settings.playingVideoCount
                thumbnailQuality.setSelection(qualities.indexOf(settings.thumbnailQuality).takeIf { it != -1 } ?: 0)
                fullscreenQuality.setSelection(qualities.indexOf(settings.fullscreenQuality).takeIf { it != -1 } ?: 0)
                enableLogsSwitch.isChecked = settings.logsEnabled
            }
        }

        with(binding) {
            preloadedVideoCount.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    viewModel.onPreloadedVideoCountChanged(value)
                }
            }
            playingVideoCount.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    viewModel.onPlayingVideoCountChanged(value)
                }
            }
            enableLogsSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onLogsEnabled(isChecked)
            }
            thumbnailQuality.adapter = thumbnailAdapter
            thumbnailAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
            thumbnailQuality.onSelectionChanged { value ->
                val qualities = resources.getStringArray(R.array.qualities)
                viewModel.onThumbnailQualityChanged(qualities[value])
            }
            fullscreenQuality.adapter = fullscreenAdapter
            fullscreenAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
            fullscreenQuality.onSelectionChanged { value ->
                val qualities = resources.getStringArray(R.array.qualities)
                viewModel.onFullscreenQualityChanged(qualities[value])
            }
            closeButton.setOnClickListener {
                dismissNow()
                viewModel.onSettingsClosed()
            }
        }
    }
}
