package com.shawnfrost.async.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumArt: String?,
    val mp3Url: String,
    val flacUrl: String?,
    val license: String,
    val source: String // FMA or IA
) 