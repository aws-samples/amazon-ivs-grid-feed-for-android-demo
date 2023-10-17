package com.amazon.ivs.gridfeed.common

import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.util.Size
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.view.doOnLayout
import com.amazon.ivs.gridfeed.R
import com.amazon.ivs.gridfeed.repository.models.GridFeedItemModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import timber.log.Timber

private const val FADE_DELAY = 250L
private const val TAG_FADE_IN = 123
private const val TAG_FADE_OUT = 456

fun View.setVisibleOr(show: Boolean = true, orWhat: Int = View.GONE) {
    alpha = 1f
    this.visibility = if (show) View.VISIBLE else orWhat
}

fun View.fadeAlpha(
    fadeIn: Boolean,
    outVisibility: Int = View.GONE
) {
    launchMain {
        delay(FADE_DELAY)
        if (fadeIn) {
            fadeIn()
        } else {
            fadeOut(outVisibility)
        }
    }
}

fun ImageView.loadImage(
    item: GridFeedItemModel,
) {
    Glide.with(context)
        .asDrawable()
        .load(item.imageUrl)
        .override(measuredWidth, measuredHeight)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun TextureView.loadVideoSurface(item: GridFeedItemModel) {
    // If item is not video or has no player - return
    if (!item.isVideo || item.gridFeedPlayer == null) return

    Timber.d("Loading video for $item, ${item.isPlaying}")
    // Wait for surface to become ready - android limitation
    onReady { surface ->
        Timber.d("Loading ready for $item, ${item.isPlaying}")
        // Set player surface and add listeners
        item.gridFeedPlayer.onSizeChanged = { size ->
            resizeVideo(size)
        }
        item.gridFeedPlayer.setSurface(surface)
    }
}

fun Spinner.onSelectionChanged(callback: (Int) -> Unit) {
    var firstSelected = false

    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            /* Ignored */
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (firstSelected) {
                callback(position)
            }
            firstSelected = true
        }
    }
}

private fun View.fadeIn() {
    if (visibility == View.VISIBLE || tag == TAG_FADE_IN) return
    Timber.d("FADE IN: $visibility, $alpha, $tag, $id")
    clearAnimation()
    visibility = View.VISIBLE
    tag = TAG_FADE_IN
    animate()
        .alphaBy(1 - alpha)
        .setDuration(resources.getInteger(R.integer.fade_duration).toLong())
        .withEndAction {
            visibility = View.VISIBLE
            alpha = 1f
            tag = null
        }.start()
}

private fun View.fadeOut(outVisibility: Int = View.GONE) {
    if (visibility == outVisibility || tag == TAG_FADE_OUT) return
    Timber.d("FADE OUT: $visibility, $alpha, $tag, $id")
    clearAnimation()
    alpha = 1f
    tag = TAG_FADE_OUT
    animate()
        .alpha(0f)
        .setDuration(resources.getInteger(R.integer.fade_duration).toLong())
        .withEndAction {
            visibility = outVisibility
            tag = null
        }.start()
}

private fun TextureView.onReady(onReady: (surface: Surface) -> Unit) = launchMain {
    waitForSurface().collect { surface ->
        Timber.d("Surface collected")
        onReady(surface)
    }
}

private fun TextureView.waitForSurface() = channelFlow {
    if (surfaceTexture != null) {
        Timber.d("Surface ready")
        this.trySend(Surface(surfaceTexture)).isSuccess
        return@channelFlow
    }
    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            surfaceTextureListener = null
            Timber.d("Surface ready")
            this@channelFlow.trySend(Surface(surfaceTexture)).isSuccess
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            /* Ignored */
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            /* Ignored */
        }
    }
    awaitClose()
}

private fun TextureView.resizeVideo(size: Size) {
    doOnLayout {
        Resources.getSystem().displayMetrics.run {
            zoomToFit(size)
        }
    }
}

private fun View.zoomToFit(videoSize: Size) {
    (parent as View).doOnLayout { useToScale ->
        val cardWidth = useToScale.measuredWidth
        val cardHeight = useToScale.measuredHeight
        val size = calculateSurfaceSize(cardWidth, cardHeight, videoSize)
        layoutParams = FrameLayout.LayoutParams(size.width, size.height).apply {
            gravity = Gravity.CENTER
        }
    }
}

private fun calculateSurfaceSize(surfaceWidth: Int, surfaceHeight: Int, videoSize: Size): Size {
    val ratioHeight = videoSize.height.toFloat() / videoSize.width.toFloat()
    val ratioWidth = videoSize.width.toFloat() / videoSize.height.toFloat()
    val isPortrait = videoSize.width < videoSize.height
    val calculatedHeight = if (isPortrait) (surfaceWidth / ratioWidth).toInt() else (surfaceWidth * ratioHeight).toInt()
    val calculatedWidth = if (isPortrait) (surfaceHeight / ratioHeight).toInt() else (surfaceHeight * ratioWidth).toInt()
    return if (calculatedWidth >= surfaceWidth) {
        Size(calculatedWidth, surfaceHeight)
    } else {
        Size(surfaceWidth, calculatedHeight)
    }
}
