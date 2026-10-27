package com.example.data

enum class MediaSourceType {
    DEVICE_LIBRARY,
    SYSTEM_PICKER,
    FOLDER_TREE,
    GOOGLE_PHOTOS
}

data class SourceCapabilities(
    val canBrowse: Boolean,
    val persistentAccess: Boolean,
    val canPreview: Boolean,
    val canReadMetadata: Boolean,
    val isLocal: Boolean,
    val canDeleteOrTrash: Boolean,
    val canOpenInSourceApp: Boolean,
    val recoveryDescription: String,
    val deletionScopeNotice: String
)

data class MediaSourceInfo(
    val id: String,
    val name: String,
    val type: MediaSourceType,
    val description: String,
    val capabilities: SourceCapabilities,
    val itemCount: Int = 0
) {
    companion object {
        val DeviceLibrary = MediaSourceInfo(
            id = "device_media",
            name = "Phone Photos & Videos",
            type = MediaSourceType.DEVICE_LIBRARY,
            description = "Full gallery on this device. System trash or confirmation is invoked for safety.",
            capabilities = SourceCapabilities(
                canBrowse = true,
                persistentAccess = true,
                canPreview = true,
                canReadMetadata = true,
                isLocal = true,
                canDeleteOrTrash = true,
                canOpenInSourceApp = true,
                recoveryDescription = "On Android 11+, discarded media goes to your Gallery's Trash/Bin for 30 days.",
                deletionScopeNotice = "Affects local originals on device storage. May sync deletions to connected cloud backups if configured in your device gallery."
            )
        )

        val SystemPicker = MediaSourceInfo(
            id = "system_picker",
            name = "System Photo Picker",
            type = MediaSourceType.SYSTEM_PICKER,
            description = "Zero permissions required. Pick specific photos or videos from device to sort.",
            capabilities = SourceCapabilities(
                canBrowse = false,
                persistentAccess = true,
                canPreview = true,
                canReadMetadata = true,
                isLocal = true,
                canDeleteOrTrash = false,
                canOpenInSourceApp = true,
                recoveryDescription = "Files remain intact. Use the review list to locate and remove in gallery.",
                deletionScopeNotice = "Review List only. The system picker does not grant direct file deletion permissions."
            )
        )

        val FolderTree = MediaSourceInfo(
            id = "folder_tree",
            name = "Custom Folder Access",
            type = MediaSourceType.FOLDER_TREE,
            description = "Target specific directories like Screenshots or Pictures via Storage Access Framework.",
            capabilities = SourceCapabilities(
                canBrowse = true,
                persistentAccess = true,
                canPreview = true,
                canReadMetadata = true,
                isLocal = true,
                canDeleteOrTrash = true,
                canOpenInSourceApp = false,
                recoveryDescription = "Documents contract deletion. Removed permanently or according to provider.",
                deletionScopeNotice = "Deletes selected files in the user-granted folder."
            )
        )

        val GooglePhotos = MediaSourceInfo(
            id = "google_photos",
            name = "Google Photos Selection",
            type = MediaSourceType.GOOGLE_PHOTOS,
            description = "Cloud picker items. Marked removals are saved as a review list (no direct remote delete).",
            capabilities = SourceCapabilities(
                canBrowse = false,
                persistentAccess = false,
                canPreview = true,
                canReadMetadata = true,
                isLocal = false,
                canDeleteOrTrash = false,
                canOpenInSourceApp = true,
                recoveryDescription = "Cloud originals are unaffected. Deletions must be performed in Google Photos app.",
                deletionScopeNotice = "Read-only cloud selection. Deletion requires opening the Google Photos app."
            )
        )
    }
}
