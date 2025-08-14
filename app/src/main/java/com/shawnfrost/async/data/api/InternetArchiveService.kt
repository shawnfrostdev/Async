package com.shawnfrost.async.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface InternetArchiveService {
    @GET("advancedsearch.php")
    suspend fun searchAudio(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf("identifier", "title", "creator", "format"),
        @Query("rows") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json"
    ): SearchResponse

    data class SearchResponse(
        val response: Response
    ) {
        data class Response(
            val numFound: Int,
            val start: Int,
            val docs: List<Document>
        ) {
            data class Document(
                val identifier: String,
                val title: String,
                val creator: String?,
                val format: List<String>?
            )
        }
    }
} 