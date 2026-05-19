package io.videoplyr.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import android.widget.TextView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.SubtitleView
import androidx.media3.ui.TimeBar
import androidx.media3.exoplayer.ExoPlayer

class PlayerController(
    private val context: Context,
    private val surfaceView: SurfaceView,
    private val subtitleView: SubtitleView,
    private val timeBar: DefaultTimeBar,
    private val tvTime: TextView,
    private val onPlayingChanged: (Boolean) -> Unit,
    private val onTracksChanged: (hasSubs: Boolean, qualities: List<Int>) -> Unit
) {
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .build()

    private val handler = Handler(Looper.getMainLooper())

    init {
        player.setVideoSurfaceView(surfaceView)
        subtitleView.setPlayer(player)

        timeBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(t: TimeBar, pos: Long) {}
            override fun onScrubMove(t: TimeBar, pos: Long) {}
            override fun onScrubStop(t: TimeBar, pos: Long, canceled: Boolean) {
                if (!canceled) player.seekTo(pos)
            }
        })

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = onPlayingChanged(isPlaying)
            override fun onTracksChanged(tracks: Tracks) {
                val qualities = tracks.groups
                    .filter { it.type == C.TRACK_TYPE_VIDEO }
                    .flatMap { g -> (0 until g.length).map { g.getTrackFormat(it).height } }
                    .distinct().sorted()
                val hasSubs = tracks.groups.any { it.type == C.TRACK_TYPE_TEXT }
                onTracksChanged(hasSubs, qualities)
            }
        })

        val tick = object : Runnable {
            override fun run() {
                val pos = player.currentPosition
                val dur = player.duration.takeIf { it > 0 } ?: 0
                timeBar.setPosition(pos)
                timeBar.setDuration(dur)
                timeBar.setBufferedPosition(player.bufferedPosition)
                tvTime.text = "${fmt(pos)} / ${fmt(dur)}"
                handler.postDelayed(this, 500)
            }
        }
        handler.post(tick)
    }

    fun loadUrl(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    fun loadDefault() = loadUrl(
        "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-1080p.mp4"
    )

    fun loadPlaylist(items: List<PlaylistItem>) {
        player.setMediaItems(items.map { MediaItem.fromUri(it.url) })
        player.prepare()
        player.playWhenReady = true
    }

    fun toggleMute(): Boolean {
        val muting = player.volume > 0f
        player.volume = if (muting) 0f else 1f
        return muting
    }

    fun toggleCaptions(on: Boolean) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .apply {
                if (on) setPreferredTextLanguages("en", "ar")
                else setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            }.build()
    }

    fun setSpeed(speed: Float) = player.setPlaybackSpeed(speed)

    fun setQuality(height: Int) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setMaxVideoSize(Int.MAX_VALUE, height)
            .setMinVideoSize(0, height)
            .build()
    }

    fun release() { handler.removeCallbacksAndMessages(null); player.release() }

    private fun fmt(ms: Long) = "%d:%02d".format(ms / 1000 / 60, ms / 1000 % 60)
}
