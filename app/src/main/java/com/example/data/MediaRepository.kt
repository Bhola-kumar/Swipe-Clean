package com.example.data

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

data class AlbumInfo(
    val name: String,
    val totalCount: Int,
    val photoCount: Int,
    val videoCount: Int,
    val coverUri: String? = null
)

sealed class DeletionResult {
    data class Success(val batch: RemovalBatchEntity) : DeletionResult()
    data class RequiresNativePermission(val intentSender: PendingIntent, val batchId: String) : DeletionResult()
    data class Partial(val batch: RemovalBatchEntity, val failedCount: Int) : DeletionResult()
    data class ReviewOnlyNotice(val message: String, val batch: RemovalBatchEntity) : DeletionResult()
    data class Error(val message: String) : DeletionResult()
}

class MediaRepository(
    private val context: Context,
    private val dao: MediaDao
) {

    val activeSessionFlow: Flow<SessionEntity?> = dao.getLatestActiveSessionFlow()
    val allActiveSessionsFlow: Flow<List<SessionEntity>> = dao.getAllActiveSessionsFlow()
    val allSessionsFlow: Flow<List<SessionEntity>> = dao.getAllSessionsFlow()
    val allPendingThrowFlow: Flow<List<ItemWithAsset>> = dao.getAllPendingThrowItemsFlow()
    val totalReviewedFlow: Flow<Int> = dao.getTotalUniqueReviewedCountFlow()
    val totalKeptFlow: Flow<Int> = dao.getTotalKeptCountFlow()
    val totalPendingThrowFlow: Flow<Int> = dao.getTotalPendingThrowCountFlow()
    val totalBytesFreedFlow: Flow<Long> = dao.getTotalBytesFreedFlow()
    val allBatchesFlow: Flow<List<RemovalBatchEntity>> = dao.getAllBatchesFlow()

    fun getSessionItemsFlow(sessionId: String): Flow<List<ItemWithAsset>> {
        return dao.getSessionItemsWithAssetsFlow(sessionId)
    }

    suspend fun getDeviceAlbums(): List<AlbumInfo> = withContext(Dispatchers.IO) {
        val albumsMap = mutableMapOf<String, Triple<Int, Int, String?>>() // name -> (photos, videos, coverUri)

        try {
            // Images
            val imageProjection = arrayOf(
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media._ID
            )
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val bucketName = if (bucketColumn >= 0) cursor.getString(bucketColumn) ?: "Other" else "Other"
                    val id = if (idColumn >= 0) cursor.getLong(idColumn) else 0L
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val current = albumsMap[bucketName] ?: Triple(0, 0, uri)
                    albumsMap[bucketName] = Triple(current.first + 1, current.second, current.third ?: uri)
                }
            }

            // Videos
            val videoProjection = arrayOf(
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media._ID
            )
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val bucketColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                while (cursor.moveToNext()) {
                    val bucketName = if (bucketColumn >= 0) cursor.getString(bucketColumn) ?: "Other" else "Other"
                    val id = if (idColumn >= 0) cursor.getLong(idColumn) else 0L
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val current = albumsMap[bucketName] ?: Triple(0, 0, uri)
                    albumsMap[bucketName] = Triple(current.first, current.second + 1, current.third ?: uri)
                }
            }
        } catch (_: Exception) {
            // Permission restricted or empty
        }

        // Only return real albums discovered from MediaStore; no dummy hardcoded data
        albumsMap.map { (name, stats) ->
            AlbumInfo(
                name = name,
                totalCount = stats.first + stats.second,
                photoCount = stats.first,
                videoCount = stats.second,
                coverUri = stats.third
            )
        }.sortedByDescending { it.totalCount }
    }

    suspend fun queryDeviceMedia(
        albumName: String? = null,
        filterType: FilterType = FilterType.ALL,
        sortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST,
        excludeFavorites: Boolean = false,
        maxItems: Int? = null
    ): List<MediaAssetEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaAssetEntity>()

        try {
            if (filterType != FilterType.VIDEOS_ONLY) {
                val imageProjection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MediaStore.Images.Media.IS_FAVORITE else MediaStore.Images.Media._ID
                )

                val selectionArgs = mutableListOf<String>()
                var selection: String? = null
                if (!albumName.isNullOrEmpty() && albumName != "All") {
                    selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
                    selectionArgs.add(albumName)
                }

                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageProjection,
                    selection,
                    selectionArgs.toTypedArray(),
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameCol = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                    val sizeCol = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
                    val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                    val bucketCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val favCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        cursor.getColumnIndex(MediaStore.Images.Media.IS_FAVORITE)
                    } else -1

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Image_$id" else "Image_$id"
                        val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "image/jpeg" else "image/jpeg"
                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                        val width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                        val height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                        val bucket = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "Camera" else "Camera"
                        val isFav = favCol >= 0 && cursor.getInt(favCol) != 0
                        val isScreenshot = name.contains("screenshot", ignoreCase = true) || bucket.contains("screenshot", ignoreCase = true)

                        if (excludeFavorites && isFav) continue
                        if (filterType == FilterType.SCREENSHOTS && !isScreenshot) continue
                        if (filterType == FilterType.LARGE_ITEMS && size < 5_000_000L) continue

                        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString()
                        list.add(
                            MediaAssetEntity(
                                id = "device_img_$id",
                                sourceId = MediaSourceInfo.DeviceLibrary.id,
                                uriString = uri,
                                displayName = name,
                                mimeType = mime,
                                isVideo = false,
                                durationMs = 0L,
                                sizeBytes = size,
                                dateAdded = date,
                                width = width,
                                height = height,
                                isFavorite = isFav,
                                isScreenshot = isScreenshot,
                                albumName = bucket
                            )
                        )
                    }
                }
            }

            if (filterType != FilterType.PHOTOS_ONLY) {
                val videoProjection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.MIME_TYPE,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.HEIGHT,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MediaStore.Video.Media.IS_FAVORITE else MediaStore.Video.Media._ID
                )

                val selectionArgs = mutableListOf<String>()
                var selection: String? = null
                if (!albumName.isNullOrEmpty() && albumName != "All") {
                    selection = "${MediaStore.Video.Media.BUCKET_DISPLAY_NAME} = ?"
                    selectionArgs.add(albumName)
                }

                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoProjection,
                    selection,
                    selectionArgs.toTypedArray(),
                    "${MediaStore.Video.Media.DATE_ADDED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameCol = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                    val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                    val dateCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                    val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                    val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                    val durCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    val favCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        cursor.getColumnIndex(MediaStore.Video.Media.IS_FAVORITE)
                    } else -1

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "Video_$id" else "Video_$id"
                        val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "video/mp4" else "video/mp4"
                        val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                        val date = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                        val width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                        val height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                        val dur = if (durCol >= 0) cursor.getLong(durCol) else 0L
                        val bucket = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "Videos" else "Videos"
                        val isFav = favCol >= 0 && cursor.getInt(favCol) != 0

                        if (excludeFavorites && isFav) continue
                        if (filterType == FilterType.SCREENSHOTS) continue
                        if (filterType == FilterType.LARGE_ITEMS && size < 10_000_000L) continue

                        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id).toString()
                        list.add(
                            MediaAssetEntity(
                                id = "device_vid_$id",
                                sourceId = MediaSourceInfo.DeviceLibrary.id,
                                uriString = uri,
                                displayName = name,
                                mimeType = mime,
                                isVideo = true,
                                durationMs = dur,
                                sizeBytes = size,
                                dateAdded = date,
                                width = width,
                                height = height,
                                isFavorite = isFav,
                                isScreenshot = false,
                                albumName = bucket
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Permission not granted or empty
        }

        // Interleave images and videos naturally according to user's desired sort order
        val sorted = when (sortOrder) {
            MediaSortOrder.NEWEST_FIRST -> list.sortedByDescending { it.dateAdded }
            MediaSortOrder.OLDEST_FIRST -> list.sortedBy { it.dateAdded }
            MediaSortOrder.LARGEST_FIRST -> list.sortedByDescending { it.sizeBytes }
            MediaSortOrder.SHUFFLE -> list.shuffled()
        }

        if (maxItems != null && maxItems > 0) sorted.take(maxItems) else sorted
    }

    suspend fun startNewSession(
        sourceId: String,
        title: String,
        filterType: FilterType,
        assets: List<MediaAssetEntity>
    ): String = withContext(Dispatchers.IO) {
        val sessionId = UUID.randomUUID().toString()
        val session = SessionEntity(
            id = sessionId,
            sourceId = sourceId,
            title = title,
            filterType = filterType.name,
            totalCount = assets.size,
            reviewedCount = 0,
            keptCount = 0,
            throwCount = 0,
            skippedCount = 0,
            estimatedPendingBytes = 0L,
            createdAt = System.currentTimeMillis(),
            lastActivityAt = System.currentTimeMillis(),
            isCompleted = false
        )

        dao.insertAssets(assets)
        dao.insertSession(session)

        val items = assets.mapIndexed { index, asset ->
            SessionItemEntity(
                sessionId = sessionId,
                assetId = asset.id,
                position = index,
                decision = DecisionType.NONE.name,
                decidedAt = 0L
            )
        }
        dao.insertSessionItems(items)

        sessionId
    }

    suspend fun resolveSystemPickerAssets(uris: List<Uri>): List<MediaAssetEntity> = withContext(Dispatchers.IO) {
        uris.mapIndexed { index, uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val rawMime = context.contentResolver.getType(uri)
            val mimeType = rawMime ?: "application/octet-stream"
            val isVideo = mimeType.startsWith("video/", ignoreCase = true)

            var displayName = "Unavailable"
            var sizeBytes = 0L

            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameCol = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeCol = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameCol >= 0) {
                            cursor.getString(nameCol)?.let { if (it.isNotBlank()) displayName = it }
                        }
                        if (sizeCol >= 0 && !cursor.isNull(sizeCol)) {
                            sizeBytes = cursor.getLong(sizeCol)
                        }
                    }
                }
            } catch (_: Exception) {}

            var width = 0
            var height = 0
            var durationMs = 0L

            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val w = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?: retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)
                val h = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?: retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT)
                val dur = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)

                w?.toIntOrNull()?.let { width = it }
                h?.toIntOrNull()?.let { height = it }
                dur?.toLongOrNull()?.let { durationMs = it }
                retriever.release()
            } catch (_: Exception) {}

            val now = System.currentTimeMillis()
            val stableId = "picker_${uri.hashCode()}_${index}"
            MediaAssetEntity(
                id = stableId,
                sourceId = MediaSourceInfo.SystemPicker.id,
                uriString = uri.toString(),
                displayName = displayName,
                mimeType = mimeType,
                isVideo = isVideo,
                durationMs = durationMs,
                sizeBytes = sizeBytes,
                dateAdded = now / 1000,
                width = width,
                height = height,
                isFavorite = false,
                isScreenshot = false,
                albumName = "System Picker"
            )
        }
    }

    suspend fun recordDecision(
        sessionId: String,
        assetId: String,
        decision: DecisionType
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val prevDecision = dao.getItemDecision(sessionId, assetId) ?: DecisionType.NONE.name
        dao.recordDecisionTransaction(sessionId, assetId, prevDecision, decision.name, now)
        refreshSessionStats(sessionId)
    }

    suspend fun undoLastDecision(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        val lastEvent = dao.undoLastDecisionTransaction(sessionId, System.currentTimeMillis()) ?: return@withContext false
        refreshSessionStats(sessionId)
        true
    }

    suspend fun changeItemDecision(
        sessionId: String,
        assetId: String,
        newDecision: DecisionType
    ) = withContext(Dispatchers.IO) {
        dao.updateItemDecision(sessionId, assetId, newDecision.name, System.currentTimeMillis())
        refreshSessionStats(sessionId)
    }

    suspend fun refreshSessionStats(sessionId: String): Unit = withContext(Dispatchers.IO) {
        val session = dao.getSessionById(sessionId) ?: return@withContext
        val reviewed = dao.getSessionReviewedCount(sessionId)
        val kept = dao.getSessionKeptCount(sessionId)
        val thrown = dao.getSessionThrowCount(sessionId)
        val skipped = dao.getSessionSkippedCount(sessionId)
        val pendingBytes = dao.getSessionPendingBytes(sessionId)
        val isCompleted = reviewed >= session.totalCount && session.totalCount > 0

        dao.updateSession(
            session.copy(
                reviewedCount = reviewed,
                keptCount = kept,
                throwCount = thrown,
                skippedCount = skipped,
                estimatedPendingBytes = pendingBytes,
                lastActivityAt = System.currentTimeMillis(),
                isCompleted = isCompleted
            )
        )
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        dao.deleteSessionItems(sessionId)
        dao.deleteDecisionHistoryForSession(sessionId)
        dao.deleteSession(sessionId)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        dao.clearAllSessions()
        dao.clearAllSessionItems()
        dao.clearAllDecisionHistory()
        dao.clearAllBatches()
    }

    suspend fun processPendingBatch(
        sourceId: String,
        sessionId: String?,
        itemsToDelete: List<ItemWithAsset>
    ): DeletionResult = withContext(Dispatchers.IO) {
        if (itemsToDelete.isEmpty()) {
            return@withContext DeletionResult.Error("No items selected for removal.")
        }

        val batchId = UUID.randomUUID().toString()
        val totalBytes = itemsToDelete.sumOf { it.sizeBytes }

        when (sourceId) {
            MediaSourceInfo.DeviceLibrary.id, MediaSourceInfo.FolderTree.id -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val uris = itemsToDelete.mapNotNull {
                        try { Uri.parse(it.uriString) } catch (_: Exception) { null }
                    }
                    try {
                        val pendingIntent = MediaStore.createTrashRequest(context.contentResolver, uris, true)
                        DeletionResult.RequiresNativePermission(pendingIntent, batchId)
                    } catch (e: Exception) {
                        DeletionResult.Error("Unable to create system trash request: ${e.message}")
                    }
                } else {
                    var successCount = 0
                    var failCount = 0
                    var freed = 0L
                    itemsToDelete.forEach { item ->
                        try {
                            val uri = Uri.parse(item.uriString)
                            val deleted = context.contentResolver.delete(uri, null, null)
                            if (deleted > 0) {
                                successCount++
                                freed += item.sizeBytes
                                dao.updateItemDecision(item.sessionId, item.assetId, DecisionType.NONE.name, 0L)
                            } else {
                                failCount++
                            }
                        } catch (_: Exception) {
                            failCount++
                        }
                    }
                    val batch = RemovalBatchEntity(
                        id = batchId,
                        sessionId = sessionId,
                        sourceId = sourceId,
                        timestamp = System.currentTimeMillis(),
                        requestedCount = itemsToDelete.size,
                        succeededCount = successCount,
                        failedCount = failCount,
                        canceledCount = 0,
                        bytesFreed = freed,
                        status = if (failCount == 0) BatchStatus.SUCCEEDED.name else BatchStatus.PARTIAL.name,
                        note = "Direct ContentResolver deletion."
                    )
                    dao.insertBatch(batch)
                    if (failCount > 0) DeletionResult.Partial(batch, failCount) else DeletionResult.Success(batch)
                }
            }

            MediaSourceInfo.SystemPicker.id, MediaSourceInfo.GooglePhotos.id -> {
                val batch = RemovalBatchEntity(
                    id = batchId,
                    sessionId = sessionId,
                    sourceId = sourceId,
                    timestamp = System.currentTimeMillis(),
                    requestedCount = itemsToDelete.size,
                    succeededCount = 0,
                    failedCount = 0,
                    canceledCount = 0,
                    bytesFreed = 0L,
                    status = BatchStatus.SUCCEEDED.name,
                    note = "Review completed. Originals remain untouched in source."
                )
                dao.insertBatch(batch)
                itemsToDelete.forEach {
                    dao.updateItemDecision(it.sessionId, it.assetId, DecisionType.KEEP.name, System.currentTimeMillis())
                }
                DeletionResult.ReviewOnlyNotice(
                    "Review completed. As explained by ${if (sourceId == MediaSourceInfo.GooglePhotos.id) "Google Photos" else "System Picker"}, removals must be performed directly in the source app.",
                    batch
                )
            }

            else -> {
                DeletionResult.Error("Unsupported source.")
            }
        }
    }

    suspend fun recordNativeBatchOutcome(
        batchId: String,
        sourceId: String,
        sessionId: String?,
        items: List<ItemWithAsset>,
        isApproved: Boolean
    ) = withContext(Dispatchers.IO) {
        val totalBytes = items.sumOf { it.sizeBytes }
        val batch = RemovalBatchEntity(
            id = batchId,
            sessionId = sessionId,
            sourceId = sourceId,
            timestamp = System.currentTimeMillis(),
            requestedCount = items.size,
            succeededCount = if (isApproved) items.size else 0,
            failedCount = 0,
            canceledCount = if (isApproved) 0 else items.size,
            bytesFreed = if (isApproved) totalBytes else 0L,
            status = if (isApproved) BatchStatus.SUCCEEDED.name else BatchStatus.CANCELED.name,
            note = if (isApproved) "Moved to system trash with user confirmation." else "Canceled by user in system confirmation."
        )
        dao.insertBatch(batch)

        if (isApproved) {
            items.forEach {
                dao.updateItemDecision(it.sessionId, it.assetId, DecisionType.NONE.name, 0L)
            }
        }
    }
}
