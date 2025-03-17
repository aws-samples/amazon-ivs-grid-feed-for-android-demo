package com.amazon.ivs.gridfeed

import android.app.Application
import com.amazon.ivs.gridfeed.common.LineNumberDebugTree
import com.amazon.ivs.gridfeed.common.launchMain
import com.amazon.ivs.gridfeed.repository.GridFeedRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject
    lateinit var repository: GridFeedRepository

    private val lineNumberDebugTree = LineNumberDebugTree()

    override fun onCreate() {
        super.onCreate()
        launchMain {
            repository.settings.collectLatest { settings ->
                val isTreePlanted = Timber.forest().contains(lineNumberDebugTree)
                Timber.d("Settings updated, tree planted: $isTreePlanted")
                if (settings.logsEnabled) {
                    if (!isTreePlanted) {
                        Timber.plant(lineNumberDebugTree)
                    }
                } else {
                    if (isTreePlanted) {
                        Timber.uproot(lineNumberDebugTree)
                    }
                }
            }
        }
    }
}
