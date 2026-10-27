package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val uriString: String,
    val displayName: String,
    val mimeType: String,
    val isVideo: Boolean,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val isScreenshot: Boolean = false,
    val albumName: String = "All",
    val localPath: String? = null
) {
    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "Size unavailable"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val formattedDuration: String
        get() {
            if (!isVideo || durationMs <= 0) return ""
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}
