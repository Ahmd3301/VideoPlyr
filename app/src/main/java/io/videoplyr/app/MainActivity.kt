package io.videoplyr.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.app.PictureInPictureParams
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.media3.ui.AspectRatioFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.videoplyr.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: PlayerController
    private lateinit var controls: ControlsManager
    private lateinit var extractor: ExtractorEngine

    private var isMuted = false
    private var captionsOn = false
    private var qualities = listOf<Int>()

    private val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    )
    private var resizeIdx = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFullscreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPlayer()
        setupControls()
        setupExtractor()
        handleIntent(intent)
    }

    private fun applyFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setupExtractor() {
        extractor = ExtractorEngine(
            context = this,
            onFound = { m3u8Url ->
                hideExtractionOverlay()
                player.loadUrl(m3u8Url)
            },
            onFailed = {
                hideExtractionOverlay()
                // Show brief toast and load default
                player.loadDefault()
            }
        )
    }

    private fun showExtractionOverlay() {
        binding.extractionOverlay.visibility = View.VISIBLE
        binding.controlsOverlay.controlsRoot.alpha = 0f
    }

    private fun hideExtractionOverlay() {
        binding.extractionOverlay.visibility = View.GONE
    }

    private fun setupPlayer() {
        val c = binding.controlsOverlay
        player = PlayerController(
            context = this,
            surfaceView = binding.surfaceView,
            subtitleView = binding.subtitleView,
            timeBar = c.timeBar,
            tvTime = c.tvTime,
            onPlayingChanged = { playing ->
                c.btnPlayPause.setImageResource(
                    if (playing) R.drawable.ic_pause else R.drawable.ic_play
                )
            },
            onTracksChanged = { hasSubs, q ->
                qualities = q
                c.btnCaptions.isEnabled = hasSubs
            }
        )
    }

    private fun setupControls() {
        val c = binding.controlsOverlay
        controls = ControlsManager(c.controlsRoot, c.playlistContainer)

        c.btnPlayPause.setOnClickListener {
            if (player.player.isPlaying) player.player.pause() else player.player.play()
            controls.scheduleHide()
        }
        c.btnRewind.setOnClickListener { player.player.seekBack(); controls.scheduleHide() }
        c.btnForward.setOnClickListener { player.player.seekForward(); controls.scheduleHide() }
        c.btnMute.setOnClickListener {
            isMuted = player.toggleMute()
            c.btnMute.setImageResource(if (isMuted) R.drawable.ic_mute else R.drawable.ic_volume)
            controls.scheduleHide()
        }
        c.btnCaptions.setOnClickListener {
            captionsOn = !captionsOn
            player.toggleCaptions(captionsOn)
            c.btnCaptions.setImageResource(
                if (captionsOn) R.drawable.ic_captions_on else R.drawable.ic_captions_off
            )
            controls.scheduleHide()
        }
        c.btnSettings.setOnClickListener { showSettings(); controls.cancelHide() }
        c.btnPip.setOnClickListener { enterPip() }
        c.btnResize.setOnClickListener {
            resizeIdx = (resizeIdx + 1) % resizeModes.size
            binding.aspectRatioFrame.resizeMode = resizeModes[resizeIdx]
            controls.scheduleHide()
        }
        c.btnFullscreen.setOnClickListener { finish() }
        c.titleContainer.setOnClickListener { controls.togglePlaylist() }
        binding.rootContainer.setOnClickListener { controls.toggle() }

        c.playlistRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun handleIntent(intent: Intent?) {
        val result = DeepLinkHandler.parse(intent)
        val c = binding.controlsOverlay

        when (result.type) {
            DeepLinkHandler.Type.SINGLE -> {
                player.loadUrl(result.url!!)
                c.titleText.text = "${result.title} 👍"
                setPlaylist(listOf(PlaylistItem(result.title ?: "Video", result.url)))
            }
            DeepLinkHandler.Type.SINGLE_EXTRACT -> {
                showExtractionOverlay()
                c.titleText.text = "${result.title} 👍"
                extractor.extract(result.url!!)
            }
            DeepLinkHandler.Type.PLAYLIST -> {
                player.loadPlaylist(result.playlist!!)
                c.titleText.text = "${result.playlist.first().title} 👍"
                setPlaylist(result.playlist)
            }
            DeepLinkHandler.Type.NONE -> {
                player.loadDefault()
                c.titleText.text = "Video player plyr.io 👍"
                setPlaylist(listOf(
                    PlaylistItem("Video #01", "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-720p.mp4"),
                    PlaylistItem("Video #02", "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-1080p.mp4")
                ))
            }
        }
    }

    private fun setPlaylist(items: List<PlaylistItem>) {
        val c = binding.controlsOverlay
        val adapter = PlaylistAdapter(items, binding.root as ViewGroup) { item, idx ->
            player.loadUrl(item.url)
            c.titleText.text = "${item.title} 👍"
            controls.togglePlaylist()
        }
        c.playlistRecycler.adapter = adapter
    }

    private fun showSettings() {
        val dialog = BottomSheetDialog(this, R.style.FullscreenTheme)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0x1FFFFFFF)
            setPadding(32, 32, 32, 32)
        }
        // Speed options
        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
            val tv = TextView(this).apply {
                text = "${speed}x"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(16, 24, 16, 24)
                setOnClickListener { player.setSpeed(speed); dialog.dismiss() }
            }
            layout.addView(tv)
        }
        // Quality options
        qualities.forEach { q ->
            val tv = TextView(this).apply {
                text = "${q}p"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(16, 24, 16, 24)
                setOnClickListener { player.setQuality(q); dialog.dismiss() }
            }
            layout.addView(tv)
        }
        dialog.setContentView(layout)
        dialog.show()
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
            )
        }
    }

    override fun onNewIntent(intent: Intent?) { super.onNewIntent(intent); handleIntent(intent) }
    override fun onResume() { super.onResume(); applyFullscreen() }
    override fun onPause() { super.onPause(); player.player.pause() }
    override fun onDestroy() { super.onDestroy(); player.release() }
    override fun onBackPressed() { finish() }
}
