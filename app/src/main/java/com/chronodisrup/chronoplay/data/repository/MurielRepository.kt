package com.chronodisrup.chronoplay.data.repository

import com.chronodisrup.chronoplay.core.network.ApiClient
import com.chronodisrup.chronoplay.data.remote.*

class MurielRepository(baseUrl: String) {
    private val api = ApiClient.create(baseUrl)

    suspend fun search(query: String, platform: String) = api.search(query, platform, 30).items()
    suspend fun trending() = api.trending().items()
    suspend fun hot() = api.hot(12).tracks
    suspend fun stream(track: TrackDto): String {
        val response = api.streamInfo(track.url, track.id, track.platform ?: "youtube", track.title,
            track.artist, track.duration ?: 0.0, track.thumbnail)
        return response.streamUrl?.takeIf { response.success && it.isNotBlank() }
            ?: throw IllegalStateException(response.error ?: "Muriel could not resolve this track.")
    }
    suspend fun lyrics(track: TrackDto) = api.lyrics(track.title, track.artist, track.duration ?: 0.0).lyrics.orEmpty()
    suspend fun discover(history: List<TrackSeed>, favorites: List<TrackSeed>) =
        api.discover(RecommendationRequest(history = history, favorites = favorites)).tracks
    suspend fun download(track: TrackDto): String {
        val url = track.url ?: throw IllegalStateException("This track has no source URL.")
        val response = api.download(url, track.id, track.title, track.artist)
        return response.downloadUrl?.takeIf { response.success && it.isNotBlank() }
            ?: throw IllegalStateException(response.error ?: "Download failed.")
    }
    suspend fun offlineLibrary() = api.offlineLibrary().tracks
    suspend fun toolsStatus() = api.toolsStatus()
}
