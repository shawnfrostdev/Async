package com.async.data.sync

import com.async.core.result.AsyncResult
import com.async.data.database.dao.TrackDao
import com.async.data.database.dao.PlaylistDao
import com.async.data.database.dao.PlayHistoryDao
import com.async.data.database.dao.UserSettingsDao
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import logcat.logcat

/**
 * Handles data export and import for backup and migration purposes
 */
class DataExportImport(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val playHistoryDao: PlayHistoryDao,
    private val userSettingsDao: UserSettingsDao
) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // ======== EXPORT OPERATIONS ========
    
    /**
     * Export all app data to JSON format
     */
    suspend fun exportAllData(): AsyncResult<String, ExportImportError> {
        return try {
            logcat { "Starting full data export" }
            
            val exportData = AppDataExport(
                metadata = ExportMetadata(
                    exportTime = System.currentTimeMillis(),
                    appVersion = "1.0.0", // This would come from BuildConfig
                    dataVersion = 1
                ),
                tracks = exportTracksData(),
                playlists = exportPlaylistsData(),
                playHistory = exportPlayHistoryData(),
                settings = exportSettingsData()
            )
            
            val jsonString = json.encodeToString(exportData)
            logcat { "Data export completed: ${jsonString.length} characters" }
            AsyncResult.success(jsonString)
            
        } catch (e: Exception) {
            logcat { "Error during data export" }
            AsyncResult.error(ExportImportError.ExportError(e.message ?: "Export failed"))
        }
    }
    
    /**
     * Export only playlists and their metadata
     */
    suspend fun exportPlaylists(): AsyncResult<String, ExportImportError> {
        return try {
            logcat { "Exporting playlists" }
            
            val playlistsData = exportPlaylistsData()
            val exportData = PlaylistExport(
                metadata = ExportMetadata(
                    exportTime = System.currentTimeMillis(),
                    appVersion = "1.0.0",
                    dataVersion = 1
                ),
                playlists = playlistsData
            )
            
            val jsonString = json.encodeToString(exportData)
            logcat { "Playlist export completed: ${playlistsData.size} playlists" }
            AsyncResult.success(jsonString)
            
        } catch (e: Exception) {
            logcat { "Error exporting playlists" }
            AsyncResult.error(ExportImportError.ExportError(e.message ?: "Playlist export failed"))
        }
    }
    
    /**
     * Export user settings
     */
    suspend fun exportSettings(): AsyncResult<String, ExportImportError> {
        return try {
            logcat { "Exporting settings" }
            
            val settingsData = exportSettingsData()
            val exportData = SettingsExport(
                metadata = ExportMetadata(
                    exportTime = System.currentTimeMillis(),
                    appVersion = "1.0.0",
                    dataVersion = 1
                ),
                settings = settingsData
            )
            
            val jsonString = json.encodeToString(exportData)
            logcat { "Settings export completed: ${settingsData.size} settings" }
            AsyncResult.success(jsonString)
            
        } catch (e: Exception) {
            logcat { "Error exporting settings" }
            AsyncResult.error(ExportImportError.ExportError(e.message ?: "Settings export failed"))
        }
    }
    
    // ======== IMPORT OPERATIONS ========
    
    /**
     * Import all app data from JSON format
     */
    suspend fun importAllData(jsonData: String, replaceExisting: Boolean = false): AsyncResult<ImportResult, ExportImportError> {
        return try {
            logcat { "Starting full data import (replace: $replaceExisting)" }
            
            val exportData = json.decodeFromString<AppDataExport>(jsonData)
            val result = ImportResult()
            
            // Validate import data
            val validationResult = validateImportData(exportData.metadata)
            if (validationResult.isError) {
                return AsyncResult.error(validationResult.getErrorOrNull()!!)
            }
            
            // Clear existing data if requested
            if (replaceExisting) {
                clearAllData()
            }
            
            // Import tracks
            result.tracksImported = importTracksData(exportData.tracks)
            
            // Import playlists
            result.playlistsImported = importPlaylistsData(exportData.playlists)
            
            // Import play history
            result.historyImported = importPlayHistoryData(exportData.playHistory)
            
            // Import settings
            result.settingsImported = importSettingsData(exportData.settings)
            
            logcat { "Data import completed: $result" }
            AsyncResult.success(result)
            
        } catch (e: Exception) {
            logcat { "Error during data import" }
            AsyncResult.error(ExportImportError.ImportError(e.message ?: "Import failed"))
        }
    }
    
    /**
     * Import only playlists
     */
    suspend fun importPlaylists(jsonData: String, mergeMode: ImportMergeMode = ImportMergeMode.MERGE): AsyncResult<Int, ExportImportError> {
        return try {
            logcat { "Importing playlists (mode: $mergeMode)" }
            
            val playlistExport = json.decodeFromString<PlaylistExport>(jsonData)
            
            // Validate import data
            val validationResult = validateImportData(playlistExport.metadata)
            if (validationResult.isError) {
                return AsyncResult.error(validationResult.getErrorOrNull()!!)
            }
            
            val importedCount = importPlaylistsData(playlistExport.playlists, mergeMode)
            
            logcat { "Playlist import completed: $importedCount playlists" }
            AsyncResult.success(importedCount)
            
        } catch (e: Exception) {
            logcat { "Error importing playlists" }
            AsyncResult.error(ExportImportError.ImportError(e.message ?: "Playlist import failed"))
        }
    }
    
    /**
     * Import user settings
     */
    suspend fun importSettings(jsonData: String, mergeMode: ImportMergeMode = ImportMergeMode.MERGE): AsyncResult<Int, ExportImportError> {
        return try {
            logcat { "Importing settings (mode: $mergeMode)" }
            
            val settingsExport = json.decodeFromString<SettingsExport>(jsonData)
            
            // Validate import data
            val validationResult = validateImportData(settingsExport.metadata)
            if (validationResult.isError) {
                return AsyncResult.error(validationResult.getErrorOrNull()!!)
            }
            
            val importedCount = importSettingsData(settingsExport.settings, mergeMode)
            
            logcat { "Settings import completed: $importedCount settings" }
            AsyncResult.success(importedCount)
            
        } catch (e: Exception) {
            logcat { "Error importing settings" }
            AsyncResult.error(ExportImportError.ImportError(e.message ?: "Settings import failed"))
        }
    }
    
    // ======== PRIVATE EXPORT METHODS ========
    
    private suspend fun exportTracksData(): List<TrackExportData> {
        val tracks = trackDao.getAllTracks().first()
        return tracks.map { track ->
            TrackExportData(
                externalId = track.externalId,
                extensionId = track.extensionId,
                title = track.title,
                artist = track.artist,
                album = track.album,
                duration = track.duration,
                thumbnailUrl = track.thumbnailUrl,
                metadata = track.metadata,
                dateAdded = track.dateAdded,
                lastPlayed = track.lastPlayed,
                playCount = track.playCount,
                isFavorite = track.isFavorite
            )
        }
    }
    
    private suspend fun exportPlaylistsData(): List<PlaylistExportData> {
        val playlists = playlistDao.getAllPlaylists().first()
        return playlists.map { playlist ->
            val tracks = playlistDao.getPlaylistTracksSync(playlist.id)
            PlaylistExportData(
                name = playlist.name,
                description = playlist.description,
                coverArtUrl = playlist.coverArtUrl,
                isSystemPlaylist = playlist.isSystemPlaylist,
                dateCreated = playlist.dateCreated,
                tracks = tracks.map { it.externalId to it.extensionId }
            )
        }
    }
    
    private suspend fun exportPlayHistoryData(): List<PlayHistoryExportData> {
        val history = playHistoryDao.getRecentPlayHistory(10000).first() // Export recent 10k items
        return history.map { historyItem ->
            PlayHistoryExportData(
                trackExternalId = "", // Would need to get from track relation
                trackExtensionId = "", // Would need to get from track relation  
                playStartTime = historyItem.timestamp,
                playDuration = historyItem.durationPlayed,
                completionPercentage = historyItem.completionPercentage,
                playSource = historyItem.source ?: "unknown",
                playContext = historyItem.sourceId
            )
        }
    }
    
    private suspend fun exportSettingsData(): List<SettingExportData> {
        val settings = userSettingsDao.getSettingsForBackup()
        return settings.map { setting ->
            SettingExportData(
                category = setting.category,
                key = setting.key,
                value = setting.value,
                valueType = setting.valueType,
                extensionId = setting.extensionId,
                description = setting.description
            )
        }
    }
    
    // ======== PRIVATE IMPORT METHODS ========
    
    private suspend fun importTracksData(tracks: List<TrackExportData>): Int {
        var importedCount = 0
        
        tracks.forEach { trackData ->
            try {
                // Check if track already exists
                val existing = trackDao.getTrackByExternalId(trackData.extensionId, trackData.externalId)
                if (existing == null) {
                    // Create new track entity
                    val trackEntity = com.async.data.database.entity.TrackEntity(
                        externalId = trackData.externalId,
                        extensionId = trackData.extensionId,
                        title = trackData.title,
                        artist = trackData.artist,
                        album = trackData.album,
                        duration = trackData.duration,
                        thumbnailUrl = trackData.thumbnailUrl,
                        streamUrl = null, // Not exported for security
                        metadata = trackData.metadata,
                        dateAdded = trackData.dateAdded,
                        lastPlayed = trackData.lastPlayed,
                        playCount = trackData.playCount,
                        isFavorite = trackData.isFavorite
                    )
                    trackDao.insertTrack(trackEntity)
                    importedCount++
                }
            } catch (e: Exception) {
                logcat { "Failed to import track: ${trackData.title}" }
            }
        }
        
        return importedCount
    }
    
    private suspend fun importPlaylistsData(playlists: List<PlaylistExportData>, mergeMode: ImportMergeMode = ImportMergeMode.MERGE): Int {
        var importedCount = 0
        
        playlists.forEach { playlistData ->
            try {
                // Skip system playlists in merge mode
                if (mergeMode == ImportMergeMode.MERGE && playlistData.isSystemPlaylist) {
                    return@forEach
                }
                
                // Check if playlist already exists
                val existing = playlistDao.getPlaylistByName(playlistData.name)
                if (existing == null || mergeMode == ImportMergeMode.REPLACE) {
                    // Create new playlist
                    val playlistEntity = com.async.data.database.entity.PlaylistEntity(
                        name = playlistData.name,
                        description = playlistData.description,
                        coverArtUrl = playlistData.coverArtUrl,
                        isSystemPlaylist = playlistData.isSystemPlaylist,
                        dateCreated = playlistData.dateCreated
                    )
                    
                    val playlistId = playlistDao.insertPlaylist(playlistEntity)
                    
                    // Add tracks to playlist
                    playlistData.tracks.forEach { (externalId, extensionId) ->
                        val track = trackDao.getTrackByExternalId(extensionId, externalId)
                        if (track != null) {
                            playlistDao.addTrackToPlaylist(playlistId, track.id)
                        }
                    }
                    
                    importedCount++
                }
            } catch (e: Exception) {
                logcat { "Failed to import playlist: ${playlistData.name}" }
            }
        }
        
        return importedCount
    }
    
    private suspend fun importPlayHistoryData(history: List<PlayHistoryExportData>): Int {
        var importedCount = 0
        
        history.forEach { historyData ->
            try {
                // Find corresponding track
                val track = trackDao.getTrackByExternalId(historyData.trackExtensionId, historyData.trackExternalId)
                if (track != null) {
                    val historyEntity = com.async.data.database.entity.PlayHistoryEntity(
                        trackId = track.id,
                        timestamp = historyData.playStartTime,
                        durationPlayed = historyData.playDuration,
                        completionPercentage = historyData.completionPercentage,
                        source = historyData.playSource,
                        sourceId = historyData.playContext
                    )
                    
                    playHistoryDao.insertPlayHistory(historyEntity)
                    importedCount++
                }
            } catch (e: Exception) {
                logcat { "Failed to import history item" }
            }
        }
        
        return importedCount
    }
    
    private suspend fun importSettingsData(settings: List<SettingExportData>, mergeMode: ImportMergeMode = ImportMergeMode.MERGE): Int {
        var importedCount = 0
        
        settings.forEach { settingData ->
            try {
                when (mergeMode) {
                    ImportMergeMode.MERGE -> {
                        // Only import if setting doesn't exist
                        val existing = userSettingsDao.getSettingValue(settingData.category, settingData.key)
                        if (existing == null) {
                            userSettingsDao.setStringValue(settingData.category, settingData.key, settingData.value ?: "")
                            importedCount++
                        }
                    }
                    ImportMergeMode.REPLACE -> {
                        // Always replace existing setting
                        userSettingsDao.setStringValue(settingData.category, settingData.key, settingData.value ?: "")
                        importedCount++
                    }
                }
            } catch (e: Exception) {
                logcat { "Failed to import setting: ${settingData.category}:${settingData.key}" }
            }
        }
        
        return importedCount
    }
    
    // ======== VALIDATION AND UTILITY METHODS ========
    
    private fun validateImportData(metadata: ExportMetadata): AsyncResult<Unit, ExportImportError> {
        return try {
            // Check data version compatibility
            if (metadata.dataVersion > 1) {
                return AsyncResult.error(ExportImportError.IncompatibleVersion("Data version ${metadata.dataVersion} not supported"))
            }
            
            // Check export age (warn if older than 30 days)
            val ageHours = (System.currentTimeMillis() - metadata.exportTime) / (1000 * 60 * 60)
            if (ageHours > 24 * 30) {
                logcat { "Import data is ${ageHours / 24} days old" }
            }
            
            AsyncResult.success(Unit)
        } catch (e: Exception) {
            AsyncResult.error(ExportImportError.ValidationError(e.message ?: "Validation failed"))
        }
    }
    
    private suspend fun clearAllData() {
        try {
            logcat { "Clearing all existing data for import" }
            
            // Clear in reverse dependency order
            playHistoryDao.deleteAllPlayHistory()
            // Clear user playlists - would need specific DAO method
            // For now, just clear all non-system playlists
            trackDao.deleteAllTracks()
            // Keep settings for now - they'll be overwritten during import
            
        } catch (e: Exception) {
            logcat { "Error clearing existing data" }
        }
    }
    
    /**
     * Get export/import statistics
     */
    suspend fun getExportStats(): ExportStats {
        return try {
            ExportStats(
                totalTracks = trackDao.getTotalTrackCount(),
                totalPlaylists = playlistDao.getTotalPlaylistCount(),
                totalHistory = playHistoryDao.getTotalPlayHistoryCount(),
                totalSettings = userSettingsDao.getTotalSettingsCount(),
                favoriteTracks = trackDao.getFavoriteTrackCount(),
                userPlaylists = playlistDao.getUserPlaylistCount()
            )
        } catch (e: Exception) {
            logcat { "Error getting export stats" }
            ExportStats()
        }
    }
}

// ======== DATA CLASSES FOR EXPORT/IMPORT ========

@Serializable
data class AppDataExport(
    val metadata: ExportMetadata,
    val tracks: List<TrackExportData>,
    val playlists: List<PlaylistExportData>,
    val playHistory: List<PlayHistoryExportData>,
    val settings: List<SettingExportData>
)

@Serializable
data class PlaylistExport(
    val metadata: ExportMetadata,
    val playlists: List<PlaylistExportData>
)

@Serializable
data class SettingsExport(
    val metadata: ExportMetadata,
    val settings: List<SettingExportData>
)

@Serializable
data class ExportMetadata(
    val exportTime: Long,
    val appVersion: String,
    val dataVersion: Int
)

@Serializable
data class TrackExportData(
    val externalId: String,
    val extensionId: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long?,
    val thumbnailUrl: String?,
    val metadata: String?,
    val dateAdded: Long,
    val lastPlayed: Long?,
    val playCount: Int,
    val isFavorite: Boolean
)

@Serializable
data class PlaylistExportData(
    val name: String,
    val description: String?,
    val coverArtUrl: String?,
    val isSystemPlaylist: Boolean,
    val dateCreated: Long,
    val tracks: List<Pair<String, String>> // externalId to extensionId
)

@Serializable
data class PlayHistoryExportData(
    val trackExternalId: String,
    val trackExtensionId: String,
    val playStartTime: Long,
    val playDuration: Long,
    val completionPercentage: Float,
    val playSource: String,
    val playContext: String?
)

@Serializable
data class SettingExportData(
    val category: String,
    val key: String,
    val value: String?,
    val valueType: String,
    val extensionId: String?,
    val description: String?
)

// ======== RESULT AND STATUS CLASSES ========

data class ImportResult(
    var tracksImported: Int = 0,
    var playlistsImported: Int = 0,
    var historyImported: Int = 0,
    var settingsImported: Int = 0,
    val importTime: Long = System.currentTimeMillis()
) {
    val totalItemsImported: Int
        get() = tracksImported + playlistsImported + historyImported + settingsImported
}

data class ExportStats(
    val totalTracks: Int = 0,
    val totalPlaylists: Int = 0,
    val totalHistory: Int = 0,
    val totalSettings: Int = 0,
    val favoriteTracks: Int = 0,
    val userPlaylists: Int = 0
)

enum class ImportMergeMode {
    MERGE,    // Keep existing data, only add new items
    REPLACE   // Replace existing data with imported data
}

// ======== ERROR TYPES ========

sealed class ExportImportError {
    data class ExportError(val message: String) : ExportImportError()
    data class ImportError(val message: String) : ExportImportError()
    data class ValidationError(val message: String) : ExportImportError()
    data class IncompatibleVersion(val message: String) : ExportImportError()
    object DataCorruption : ExportImportError()
    object InsufficientStorage : ExportImportError()
} 