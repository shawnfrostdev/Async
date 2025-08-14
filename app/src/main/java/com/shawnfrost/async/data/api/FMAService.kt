package com.shawnfrost.async.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface FMAService {
    // FMA now uses web scraping approach - these endpoints are deprecated
    // We'll implement a fallback with mock data for now
    
    @GET("music/charts/all")
    suspend fun getTrendingTracks(
        @Query("sort") sort: String = "listens",
        @Query("pageSize") limit: Int = 20
    ): FMAResponse

    @GET("music/charts/this-week") 
    suspend fun getNewReleases(
        @Query("sort") sort: String = "listens",
        @Query("pageSize") limit: Int = 20
    ): FMAResponse

    @GET("search/")
    suspend fun searchTracks(
        @Query("quicksearch") query: String,
        @Query("pageSize") limit: Int = 20
    ): FMAResponse

    data class FMAResponse(
        val tracks: List<Track> = emptyList()
    )

    data class Track(
        val track_id: String,
        val track_title: String,
        val artist_name: String,
        val track_duration: String,
        val track_image_file: String?,
        val track_url: String,
        val license_title: String
    )
} 