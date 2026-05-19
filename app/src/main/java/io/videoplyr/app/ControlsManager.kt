package io.videoplyr.app

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

class ControlsManager(
    private val controlsRoot: View,
    private val playlistContainer: View,
    private val autoHideMs: Long = 3000L
) {
    private val handler = Handler(Looper.getMainLooper())
    var isVisible = false
    var isPlaylistOpen = false
    private val hideRunnable = Runnable { hide() }

    fun show() {
        isVisible = true
        controlsRoot.animate().alpha(1f).setDuration(300)
            .setInterpolator(DecelerateInterpolator()).start()
        scheduleHide()
    }

    fun hide() {
        isVisible = false
        if (isPlaylistOpen) hidePlaylist()
        controlsRoot.animate().alpha(0f).setDuration(300)
            .setInterpolator(AccelerateInterpolator()).start()
    }

    fun toggle() { if (isVisible) hide() else show() }

    fun togglePlaylist() {
        if (isPlaylistOpen) hidePlaylist() else showPlaylist()
    }

    private fun showPlaylist() {
        isPlaylistOpen = true
        playlistContainer.visibility = View.VISIBLE
        playlistContainer.animate()
            .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
        cancelHide()
    }

    private fun hidePlaylist() {
        isPlaylistOpen = false
        val dy = 20f * playlistContainer.resources.displayMetrics.density
        playlistContainer.animate()
            .alpha(0f).translationY(dy).scaleX(0.98f).scaleY(0.98f)
            .setDuration(300)
            .withEndAction { playlistContainer.visibility = View.GONE }
            .start()
    }

    fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, autoHideMs)
    }

    fun cancelHide() = handler.removeCallbacks(hideRunnable)
}
