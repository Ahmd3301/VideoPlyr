package io.videoplyr.app

import android.content.Intent
import android.util.Base64
import org.json.JSONArray

object DeepLinkHandler {

    enum class Type { SINGLE, SINGLE_EXTRACT, PLAYLIST, NONE }

    data class Result(
        val type: Type,
        val url: String? = null,
        val title: String? = null,
        val playlist: List<PlaylistItem>? = null
    )

    fun parse(intent: Intent?): Result {
        val uri = intent?.data ?: return Result(Type.NONE)
        if (uri.scheme != "videoplyrio") return Result(Type.NONE)

        return when (uri.host) {
            "open" -> {
                val rawUrl = uri.getQueryParameter("url") ?: return Result(Type.NONE)
                val title = uri.getQueryParameter("title") ?: "Video"
                val type = if (ExtractorEngine.needsExtraction(rawUrl))
                    Type.SINGLE_EXTRACT else Type.SINGLE
                Result(type, url = rawUrl, title = title)
            }
            "playlist" -> {
                val data = uri.getQueryParameter("data") ?: return Result(Type.NONE)
                try {
                    val json = String(Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP))
                    val array = JSONArray(json)
                    val items = (0 until array.length()).map {
                        val obj = array.getJSONObject(it)
                        PlaylistItem(obj.getString("title"), obj.getString("url"))
                    }
                    Result(Type.PLAYLIST, playlist = items)
                } catch (e: Exception) {
                    Result(Type.NONE)
                }
            }
            else -> Result(Type.NONE)
        }
    }
}

data class PlaylistItem(val title: String, val url: String)
