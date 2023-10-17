package com.amazon.ivs.gridfeed.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.amazon.ivs.gridfeed.common.getCurrentFragment
import com.amazon.ivs.gridfeed.common.navController
import com.amazon.ivs.gridfeed.databinding.ActivityGridBinding
import com.amazon.ivs.gridfeed.ui.feed.GridFeedFullScreenFragment
import com.amazon.ivs.gridfeed.ui.feed.GridFeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GridFeedActivity : AppCompatActivity() {
    private val viewModel by viewModels<GridFeedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGridBinding.inflate(layoutInflater)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onSupportNavigateUp()
            }
        })
        setContentView(binding.root)
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.onBackClicked()
        when (getCurrentFragment()) {
            is GridFeedFullScreenFragment -> {
                viewModel.onBackClicked()
                navController.navigateUp()
            }
            else -> finish()
        }
        return false
    }
}
