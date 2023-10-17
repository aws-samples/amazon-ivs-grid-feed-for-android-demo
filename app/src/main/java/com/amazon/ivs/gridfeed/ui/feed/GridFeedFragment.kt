package com.amazon.ivs.gridfeed.ui.feed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazon.ivs.gridfeed.R
import com.amazon.ivs.gridfeed.common.collectLatestWithLifecycle
import com.amazon.ivs.gridfeed.common.navigate
import com.amazon.ivs.gridfeed.common.viewBinding
import com.amazon.ivs.gridfeed.databinding.FragmentGridFeedBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class GridFeedFragment : Fragment(R.layout.fragment_grid_feed) {
    private val binding by viewBinding(FragmentGridFeedBinding::bind)
    private val viewModel by activityViewModels<GridFeedViewModel>()

    private val adapter by lazy {
        GridFeedAdapter(
            onItemClicked = viewModel::onItemClicked,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            feedGrid.adapter = adapter
            feedGrid.itemAnimator = null
            feedGrid.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    onFeedScrolled(recyclerView)
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        onFeedScrolled(recyclerView)
                    }
                }
            })
            settings.setOnClickListener {
                navigate(GridFeedFragmentDirections.toSettings())
            }
        }
        collectLatestWithLifecycle(viewModel.feed) { feed ->
            val loadedVideoCount = feed.count { row -> row.views.any { it.gridFeedPlayer != null } }
            Timber.d("Feed updated: $loadedVideoCount")
            adapter.submitList(feed)
        }
        collectLatestWithLifecycle(viewModel.onSettingsUpdated) {
            binding.feedGrid.scrollToPosition(0)
            viewModel.reloadFeed()
        }
        collectLatestWithLifecycle(viewModel.onGoFullScreen) {
            navigate(GridFeedFragmentDirections.toFullScreen())
        }
    }

    private fun onFeedScrolled(recyclerView: RecyclerView) {
        val manager = recyclerView.layoutManager as LinearLayoutManager
        val firstRowId = manager.findFirstCompletelyVisibleItemPosition()
        val lastRowId = manager.findLastVisibleItemPosition()
        viewModel.onFeedScrolled(firstRowId, lastRowId)
    }
}
