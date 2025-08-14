package com.shawnfrost.async.data.api

import com.shawnfrost.async.domain.model.Track

/**
 * Mock music service to provide sample tracks when external APIs are unavailable
 * This ensures the app always has some music content to display
 */
object MockMusicService {
    
    fun getTrendingTracks(): List<Track> = listOf(
        Track(
            id = "mock_1",
            title = "Chill Hop Beat",
            artist = "LoFi Producer",
            duration = 180000L, // 3 minutes
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/Broke_For_Free/Directionless_EP/Broke_For_Free_-_01_-_Night_Owl.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        ),
        Track(
            id = "mock_2", 
            title = "Jazz Fusion",
            artist = "Smooth Collective",
            duration = 240000L, // 4 minutes
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/Broke_For_Free/Something_EP/Broke_For_Free_-_01_-_Something_Elated.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        ),
        Track(
            id = "mock_3",
            title = "Electronic Dreams",
            artist = "Synth Wave",
            duration = 200000L, // 3:20
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/ccCommunity/Tours/Enthusiast/Tours_-_01_-_Enthusiast.mp3",
            flacUrl = null,
            license = "Creative Commons", 
            source = "Demo"
        ),
        Track(
            id = "mock_4",
            title = "Acoustic Sunrise",
            artist = "Folk Artist",
            duration = 220000L, // 3:40
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/Podington_Bear/Solo_Instruments/Podington_Bear_-_Starling.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        ),
        Track(
            id = "mock_5",
            title = "Hip Hop Instrumental",
            artist = "Beat Maker",
            duration = 160000L, // 2:40
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/ccCommunity/Jahzzar/Travellers_Guide/Jahzzar_-_05_-_Siesta.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        )
    )
    
    fun getNewReleases(): List<Track> = listOf(
        Track(
            id = "mock_new_1",
            title = "Fresh Beats 2024",
            artist = "New Producer",
            duration = 195000L,
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/BoxCat_Games/Nameless_the_Hackers_RPG_Soundtrack/BoxCat_Games_-_10_-_Epic_Song.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        ),
        Track(
            id = "mock_new_2",
            title = "Latest Melody",
            artist = "Rising Star",
            duration = 210000L,
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/Chad_Crouch/Arps/Chad_Crouch_-_Algorithms.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        ),
        Track(
            id = "mock_new_3",
            title = "Modern Classic",
            artist = "Contemporary Artist",
            duration = 185000L,
            albumArt = null,
            mp3Url = "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/WFMU/The_Kyoto_Connection/Wake_Up/The_Kyoto_Connection_-_Hachiko_The_Faithtful_Dog.mp3",
            flacUrl = null,
            license = "Creative Commons",
            source = "Demo"
        )
    )
    
    fun searchTracks(query: String): List<Track> {
        val allTracks = getTrendingTracks() + getNewReleases()
        return allTracks.filter { track ->
            track.title.contains(query, ignoreCase = true) ||
            track.artist.contains(query, ignoreCase = true)
        }
    }
} 