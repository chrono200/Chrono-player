package com.chronodisrup.chronoplay.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MurielApi {
    @GET("api/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("platform") platform: String = "ytmusic",
        @Query("limit") limit: Int = 20
    ): SearchResponse

    @GET("api/trending")
    suspend fun trending(): SearchResponse
}

data class SearchResponse(
    val results: List<TrackDto> = emptyList(),
    val tracks: List<TrackDto> = emptyList()
)

data class TrackDto(
    val id: String? = null,
    val title: String = "Unknown track",
    val artist: String = "Unknown artist",
    val thumbnail: String? = null,
    val duration: Double? = null,
    val url: String? = null,
    val platform: String? = null
)
