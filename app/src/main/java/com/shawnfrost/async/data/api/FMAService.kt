package com.shawnfrost.async.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface FMAService {
    @GET("tracks")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): List<Track>

    @GET("tracks/popular")
    suspend fun getTrendingTracks(
        @Query("limit") limit: Int = 20
    ): List<Track>

    @GET("tracks/recent")
    suspend fun getNewReleases(
        @Query("limit") limit: Int = 20
    ): List<Track>

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