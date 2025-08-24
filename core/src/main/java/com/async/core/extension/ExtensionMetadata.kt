package com.async.core.extension

import kotlinx.serialization.Serializable

/**
 * Metadata information about an extension
 */
@Serializable
data class ExtensionMetadata(
    val id: String,
    val version: Int,
    val name: String,
    val developer: String,
    val description: String,
    val iconUrl: String? = null,
    val websiteUrl: String? = null,
    val filePath: String? = null,
    val installDate: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val fileSize: Long = 0,
    val checksum: String? = null,
    val versionString: String? = null
) {
    companion object {
        /**
         * Create ExtensionMetadata from a MusicExtension instance
         */
        fun fromExtension(extension: MusicExtension, filePath: String? = null): ExtensionMetadata {
            return ExtensionMetadata(
                id = extension.id,
                version = extension.version,
                name = extension.name,
                developer = extension.developer,
                description = extension.description,
                iconUrl = extension.iconUrl,
                websiteUrl = extension.websiteUrl,
                filePath = filePath,
                installDate = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Extension lifecycle status
 */
@Serializable
enum class ExtensionStatus {
    INSTALLING,
    INSTALLED,
    UPDATING,
    UPDATED,
    DISABLED,
    UNINSTALLING,
    INSTALL_FAILED,
    UPDATE_FAILED,
    INCOMPATIBLE,
    CORRUPTED
}

/**
 * Complete extension information including metadata and status
 */
@Serializable
data class ExtensionInfo(
    val metadata: ExtensionMetadata,
    val status: ExtensionStatus,
    val errorMessage: String? = null,
    val isLoaded: Boolean = false,
    val lastUsed: Long = 0,
    val usageCount: Long = 0
)

/**
 * Configuration item for extensions
 */
@Serializable
sealed class ExtensionConfigItem {
    abstract val key: String
    abstract val label: String
    abstract val description: String?
    abstract val required: Boolean
    
    @Serializable
    data class StringConfig(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: String = "",
        val placeholder: String? = null,
        val maxLength: Int? = null
    ) : ExtensionConfigItem()
    
    @Serializable
    data class BooleanConfig(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: Boolean = false
    ) : ExtensionConfigItem()
    
    @Serializable
    data class NumberConfig(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: Double = 0.0,
        val minValue: Double? = null,
        val maxValue: Double? = null,
        val step: Double? = null
    ) : ExtensionConfigItem()
    
    @Serializable
    data class SelectConfig(
        override val key: String,
        override val label: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val options: List<SelectOption>,
        val defaultValue: String? = null,
        val allowMultiple: Boolean = false
    ) : ExtensionConfigItem()
}

/**
 * Option for select configuration items
 */
@Serializable
data class SelectOption(
    val value: String,
    val label: String,
    val description: String? = null
) 