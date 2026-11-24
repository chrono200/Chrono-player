package com.chronodisrup.chronoplay.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MurielApi {
    @GET("api/search")
    suspend fun search(@Query("q") query: String, @Query("platform") platform: String = "youtube", @Query("limit") limit: Int = 20): SearchResponse
    @GET("api/trending") suspend fun trending(): SearchResponse
    @GET("api/hot") suspend fun hot(@Query("limit") limit: Int = 12): HotResponse
    @GET("api/stream-info")
    suspend fun streamInfo(@Query("url") url: String?, @Query("id") id: String?, @Query("platform") platform: String?,
        @Query("title") title: String, @Query("artist") artist: String, @Query("duration") duration: Double,
        @Query("thumbnail") thumbnail: String?): StreamInfoResponse
    @GET("api/prefetch")
    suspend fun prefetch(@Query("url") url: String?, @Query("id") id: String?, @Query("platform") platform: String?,
        @Query("title") title: String?, @Query("artist") artist: String?, @Query("duration") duration: Double?,
        @Query("thumbnail") thumbnail: String?): BasicResponse
    @GET("api/check-cache") suspend fun checkCache(@Query("id") id: String): CacheResponse
    @GET("api/download")
    suspend fun download(@Query("url") url: String, @Query("id") id: String?, @Query("title") title: String,
        @Query("artist") artist: String, @Query("format") format: String = "mp3", @Query("quality") quality: String? = null): DownloadResponse
    @GET("api/offline-library") suspend fun offlineLibrary(): OfflineLibraryResponse
    @GET("api/lyrics")
    suspend fun lyrics(@Query("title") title: String, @Query("artist") artist: String,
        @Query("duration") duration: Double = 0.0, @Query("source") source: String = "lrclib"): LyricsResponse
    @GET("api/lyrics-words")
    suspend fun lyricsWords(@Query("title") title: String, @Query("artist") artist: String, @Query("duration") duration: Double = 0.0): LyricsWordsResponse
    @POST("api/discover") suspend fun discover(@Body body: RecommendationRequest): RecommendationResponse
    @POST("api/vibe") suspend fun vibe(@Body body: RecommendationRequest): RecommendationResponse
    @GET("api/artist") suspend fun artist(@Query("name") name: String, @Query("id") id: String? = null, @Query("video") video: String? = null): ArtistResponse
    @GET("api/artist-matches") suspend fun artistMatches(@Query("name") name: String): ArtistMatchesResponse
    @GET("api/artist-albums") suspend fun artistAlbums(@Query("name") name: String, @Query("id") id: String? = null, @Query("video") video: String? = null): AlbumsResponse
    @GET("api/albums/search") suspend fun searchAlbums(@Query("q") query: String): AlbumsResponse
    @GET("api/albums/tracks") suspend fun albumTracks(@Query("id") id: String? = null, @Query("title") title: String? = null,
        @Query("artist") artist: String? = null, @Query("source") source: String = "itunes"): SearchResponse
    @GET("api/library") suspend fun serverLibrary(): List<TrackDto>
    @GET("api/tools-status") suspend fun toolsStatus(): ToolsStatusResponse
    @GET("api/track-analysis") suspend fun trackAnalysis(@Query("id") id: String): TrackAnalysisResponse
    @POST("api/track-analysis") suspend fun saveTrackAnalysis(@Body body: TrackAnalysisRequest): BasicResponse
    @POST("api/save-custom-lyrics")
    suspend fun saveCustomLyrics(@Query("title") title: String, @Query("artist") artist: String, @Body body: CustomLyricsRequest): BasicResponse
    @POST("api/translate") suspend fun translate(@Body body: TranslationRequest): TranslationResponse
    @POST("api/offline-delete") suspend fun deleteOffline(@Query("id") id: String): BasicResponse
    @POST("api/clear-offline-library") suspend fun clearOfflineLibrary(): BasicResponse
    @POST("api/library-add") suspend fun addLibraryItems(@Body body: Map<String, Any>): BasicResponse
    @POST("api/library-update") suspend fun updateLibraryItem(@Body body: Map<String, Any>): BasicResponse
    @POST("api/library-remove") suspend fun removeLibraryItem(@Body body: Map<String, Any>): BasicResponse
}

data class SearchResponse(val success: Boolean = true, val tracks: List<TrackDto> = emptyList(), val results: List<TrackDto> = emptyList(), val error: String? = null) {
    fun items(): List<TrackDto> = if (tracks.isNotEmpty()) tracks else results
}
data class HotResponse(val success: Boolean = true, val title: String? = null, val tracks: List<TrackDto> = emptyList(), val error: String? = null)
data class TrackDto(val id: String? = null, val title: String = "Unknown track", val artist: String = "Unknown artist",
    val duration: Double? = null, val thumbnail: String? = null, val url: String? = null, val platform: String? = null, val isCached: Boolean = false)
data class StreamInfoResponse(val success: Boolean = false, val isLocal: Boolean = false, val streamUrl: String? = null, val error: String? = null)
data class BasicResponse(val success: Boolean = false, val error: String? = null)
data class CacheResponse(val cached: Boolean = false, val streamUrl: String? = null)
data class DownloadResponse(val success: Boolean = false, val filename: String? = null, val downloadUrl: String? = null, val error: String? = null)
data class OfflineLibraryResponse(val success: Boolean = false, val count: Int = 0, val totalBytes: Long = 0, val tracks: List<TrackDto> = emptyList())
data class LyricsResponse(val success: Boolean = false, val lyrics: String? = null, val isCustom: Boolean = false, val error: String? = null)
data class LyricsWordsResponse(val success: Boolean = false, val lines: List<Any> = emptyList())
data class RecommendationRequest(val history: List<TrackSeed> = emptyList(), val favorites: List<TrackSeed> = emptyList(),
    val limit: Int = 12, val seeds: List<String> = emptyList(), val avoid: List<String> = emptyList(), val refresh: Boolean = false)
data class TrackSeed(val title: String, val artist: String)
data class RecommendationResponse(val success: Boolean = false, val tracks: List<TrackDto> = emptyList(), val error: String? = null)
data class ArtistResponse(val success: Boolean = false, val artist: Map<String, Any?>? = null, val error: String? = null)
data class ArtistMatchesResponse(val success: Boolean = false, val artists: List<Map<String, Any?>> = emptyList())
data class AlbumsResponse(val success: Boolean = false, val albums: List<Map<String, Any?>> = emptyList())
data class ToolsStatusResponse(val success: Boolean = false, val tools: Map<String, Any?>? = null)
data class TrackAnalysisResponse(val success: Boolean = false, val analysis: Map<String, Any?>? = null)
data class TrackAnalysisRequest(val id: String, val analysis: Map<String, Any?>)
data class CustomLyricsRequest(val lyrics: String)
data class TranslationRequest(val texts: List<String>, val targetLang: String = "en")
data class TranslationResponse(val success: Boolean = false, val translations: List<String> = emptyList(), val error: String? = null)
