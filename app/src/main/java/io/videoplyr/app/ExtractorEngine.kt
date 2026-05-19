package io.videoplyr.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class ExtractorEngine(
    private val context: Context,
    private val onFound: (String) -> Unit,
    private val onFailed: () -> Unit
) {
    // EXTRACTION MARKER — URL suffix that triggers extraction mode
    companion object {
        const val EXTRACT_SUFFIX = "###ex"
        const val TIMEOUT_MS = 10_000L
        const val POLL_INTERVAL_MS = 300L

        fun needsExtraction(url: String) = url.contains(EXTRACT_SUFFIX)
        fun cleanUrl(url: String) = url.replace(EXTRACT_SUFFIX, "")
    }

    private var extractWebView: WebView? = null
    private val handler = Handler(Looper.getMainLooper())

    // Step 1: fetch the page, find player iframe URL, then extract m3u8
    // Matches getPlayerUrl() in m3u8.js:
    //   finds li[onclick*="player_iframe.location.href"] → extracts iframe URL
    // Step 2: open iframe URL in invisible WebView, poll DOM every 300ms
    //   looking for button.hd_btn[data-url*=".m3u8"] — matches extractM3u8() in m3u8.js

    fun extract(rawUrl: String) {
        val pageUrl = cleanUrl(rawUrl)
        extractWebView?.destroy()

        val webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        }
        extractWebView = webView

        val bridge = object {
            @JavascriptInterface
            fun onM3u8Found(url: String) {
                handler.post {
                    cleanup()
                    onFound(url)
                }
            }
            @JavascriptInterface
            fun onM3u8NotFound() {
                handler.post {
                    cleanup()
                    onFailed()
                }
            }
        }
        webView.addJavascriptInterface(bridge, "Extractor")

        // Phase 1: load main page, find player iframe URL
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Inject JS that:
                // 1. Finds li[onclick*="player_iframe.location.href"]
                // 2. Extracts the iframe URL from onclick attribute
                // 3. Navigates WebView to that URL
                // 4. Then polls for button.hd_btn[data-url*=".m3u8"] every 300ms
                // 5. On find: calls Extractor.onM3u8Found(url)
                // 6. On timeout (10s): calls Extractor.onM3u8NotFound()
                view.evaluateJavascript("""
                    (function() {
                        // Check if we're already on the player page
                        var btns = document.querySelectorAll('button.hd_btn');
                        if (btns.length > 0) {
                            // Already on player page — start polling
                            startPolling();
                            return;
                        }

                        // Find player iframe URL (Step 1 from m3u8.js)
                        var li = document.querySelector('li[onclick*="player_iframe.location.href"]');
                        if (li) {
                            var onclick = li.getAttribute('onclick');
                            var match = onclick.match(/'([^']+)'/);
                            if (match) {
                                window.location.href = match[1];
                                return;
                            }
                        }

                        // If no li found, try polling directly
                        startPolling();

                        function startPolling() {
                            var attempts = 0;
                            var maxAttempts = Math.floor(10000 / 300);
                            var interval = setInterval(function() {
                                attempts++;
                                var buttons = document.querySelectorAll('button.hd_btn');
                                for (var i = 0; i < buttons.length; i++) {
                                    var dataUrl = buttons[i].getAttribute('data-url');
                                    if (dataUrl && dataUrl.indexOf('.m3u8') !== -1) {
                                        clearInterval(interval);
                                        Extractor.onM3u8Found(dataUrl);
                                        return;
                                    }
                                }
                                // Also check video src and source tags
                                var video = document.querySelector('video[src*=".m3u8"]');
                                if (video) {
                                    clearInterval(interval);
                                    Extractor.onM3u8Found(video.src);
                                    return;
                                }
                                var source = document.querySelector('source[src*=".m3u8"]');
                                if (source) {
                                    clearInterval(interval);
                                    Extractor.onM3u8Found(source.src);
                                    return;
                                }
                                if (attempts >= maxAttempts) {
                                    clearInterval(interval);
                                    Extractor.onM3u8NotFound();
                                }
                            }, 300);
                        }
                    })();
                """, null)
            }
        }

        // Load with Referer header matching m3u8.js
        webView.loadUrl(pageUrl, mapOf("Referer" to "https://faselhd.center/"))
    }

    private fun cleanup() {
        extractWebView?.destroy()
        extractWebView = null
    }
}
