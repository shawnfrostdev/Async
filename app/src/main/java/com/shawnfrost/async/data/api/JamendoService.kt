package com.shawnfrost.async.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoService {
    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String = "56d30c95", // Public demo client ID
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("search") query: String,
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse

    @GET("tracks/")
    suspend fun getTrendingTracks(
        @Query("client_id") clientId: String = "56d30c95",
        @Query("format") format: String = "json", 
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "popularity_total",
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse

    @GET("tracks/")
    suspend fun getNewReleases(
        @Query("client_id") clientId: String = "56d30c95",
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("order") order: String = "releasedate_desc",
        @Query("include") include: String = "musicinfo"
    ): JamendoResponse

    data class JamendoResponse(
        val headers: Headers,
        val results: List<Track>
    )

    data class Headers(
        val status: String,
        val code: Int,
        val error_message: String?,
        val warnings: String?,
        val results_count: Int
    )

    data class Track(
        val id: String,
        val name: String,
        val duration: Int,
        val artist_id: String,
        val artist_name: String,
        val album_id: String,
        val album_name: String,
        val album_image: String?,
        val track_image: String?,
        val audio: String,
        val audiodownload: String,
        val prourl: String?,
        val shorturl: String,
        val shareurl: String,
        val waveform: String?,
        val license_ccurl: String,
        val position: Int,
        val releasedate: String,
        val album_releasedate: String,
        val musicinfo: MusicInfo?
    )

    data class MusicInfo(
        val vocalinstrumental: String?,
        val lang: String?,
        val gender: String?,
        val speed: String?,
        val durationunit: String?,
        val tags: Tags?
    )

    data class Tags(
        val genres: List<String>?,
        val instruments: List<String>?,
        val vartags: List<String>?
    )
} 