package com.chronodisrup.chronoplay.data.repository

import com.chronodisrup.chronoplay.core.network.ApiClient
import com.chronodisrup.chronoplay.data.remote.*
import java.net.URI

class MurielRepository(private val baseUrl: String, token: String) {
    private val api = ApiClient.create(baseUrl, token)

    suspend fun search(query: String, platform: String) = api.search(query, platform, 30).items()
    suspend fun trending() = api.trending().items()
    suspend fun hot() = api.hot(12).tracks

    suspend fun stream(track: TrackDto): String {
        val response = api.streamInfo(track.url, track.id, track.platform ?: "youtube", track.title,
            track.artist, track.duration ?: 0.0, track.thumbnail)
        val raw = response.streamUrl?.takeIf { response.success && it.isNotBlank() }
            ?: throw IllegalStateException(response.error ?: "Muriel could not resolve this track.")
        return normalizeServerMediaUrl(raw)
    }

    // Muriel intentionally returns loopback URLs for files it cached on its own machine.
    // A phone must address that same path on the configured Muriel host, not its own localhost.
    private fun normalizeServerMediaUrl(raw: String): String {
        val uri = URI(raw)
        val host = uri.host?.lowercase()
        if (host != "127.0.0.1" && host != "localhost" && host != "0.0.0.0") return raw
        val base = URI(baseUrl)
        val pathAndQuery = uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
        return base.resolve(pathAndQuery).toString()
    }

    suspend fun lyrics(track: TrackDto) = api.lyrics(track.title, track.artist, track.duration ?: 0.0).lyrics.orEmpty()
    suspend fun discover(history: List<TrackSeed>, favorites: List<TrackSeed>) =
        api.discover(RecommendationRequest(history = history, favorites = favorites)).tracks
    suspend fun download(track: TrackDto): DownloadResponse {
        val url = track.url ?: throw IllegalStateException("This track has no source URL.")
        val response = api.download(url, track.id, track.title, track.artist)
        if (!response.success || response.downloadUrl.isNullOrBlank()) {
            throw IllegalStateException(response.error ?: "Download failed.")
        }
        return response
    }
    suspend fun offlineLibrary() = api.offlineLibrary().tracks
    suspend fun toolsStatus() = api.toolsStatus()
}