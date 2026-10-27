package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ItemWithAsset(
    val id: Long,
    val sessionId: String,
    val assetId: String,
    val position: Int,
    val decision: String,
    val decidedAt: Long,
    val sourceId: String,
    val uriString: String,
    val displayName: String,
    val mimeType: String,
    val isVideo: Boolean,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAdded: Long,
    val width: Int,
    val height: Int,
    val isFavorite: Boolean,
    val isScreenshot: Boolean,
    val albumName: String
)

@Dao
interface MediaDao {

    // --- Assets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<MediaAssetEntity>)

    @Query("SELECT * FROM media_assets WHERE id = :id")
    suspend fun getAssetById(id: String): MediaAssetEntity?

    @Query("SELECT * FROM media_assets WHERE sourceId = :sourceId")
    suspend fun getAssetsBySource(sourceId: String): List<MediaAssetEntity>

    // --- Sessions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE isCompleted = 0 ORDER BY lastActivityAt DESC LIMIT 1")
    fun getLatestActiveSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE isCompleted = 0 ORDER BY lastActivityAt DESC")
    fun getAllActiveSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Query("SELECT COUNT(*) FROM session_items WHERE sessionId = :sessionId AND decision != 'NONE'")
    suspend fun getSessionReviewedCount(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM session_items WHERE sessionId = :sessionId AND decision = 'KEEP'")
    suspend fun getSessionKeptCount(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM session_items WHERE sessionId = :sessionId AND decision = 'THROW'")
    suspend fun getSessionThrowCount(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM session_items WHERE sessionId = :sessionId AND decision = 'SKIP'")
    suspend fun getSessionSkippedCount(sessionId: String): Int

    @Query("""
        SELECT COALESCE(SUM(media_assets.sizeBytes), 0)
        FROM session_items
        INNER JOIN media_assets ON session_items.assetId = media_assets.id
        WHERE session_items.sessionId = :sessionId AND session_items.decision = 'THROW'
    """)
    suspend fun getSessionPendingBytes(sessionId: String): Long

    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    // --- Session Items with Assets ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionItems(items: List<SessionItemEntity>)

    @Query("UPDATE session_items SET decision = :decision, decidedAt = :decidedAt WHERE sessionId = :sessionId AND assetId = :assetId")
    suspend fun updateItemDecision(sessionId: String, assetId: String, decision: String, decidedAt: Long)

    @Query("""
        SELECT session_items.id, session_items.sessionId, session_items.assetId, session_items.position,
               session_items.decision, session_items.decidedAt,
               media_assets.sourceId, media_assets.uriString, media_assets.displayName, media_assets.mimeType,
               media_assets.isVideo, media_assets.durationMs, media_assets.sizeBytes, media_assets.dateAdded,
               media_assets.width, media_assets.height, media_assets.isFavorite, media_assets.isScreenshot,
               media_assets.albumName
        FROM session_items
        INNER JOIN media_assets ON session_items.assetId = media_assets.id
        WHERE session_items.sessionId = :sessionId
        ORDER BY session_items.position ASC
    """)
    fun getSessionItemsWithAssetsFlow(sessionId: String): Flow<List<ItemWithAsset>>

    @Query("""
        SELECT session_items.id, session_items.sessionId, session_items.assetId, session_items.position,
               session_items.decision, session_items.decidedAt,
               media_assets.sourceId, media_assets.uriString, media_assets.displayName, media_assets.mimeType,
               media_assets.isVideo, media_assets.durationMs, media_assets.sizeBytes, media_assets.dateAdded,
               media_assets.width, media_assets.height, media_assets.isFavorite, media_assets.isScreenshot,
               media_assets.albumName
        FROM session_items
        INNER JOIN media_assets ON session_items.assetId = media_assets.id
        WHERE session_items.decision = 'THROW'
        ORDER BY session_items.decidedAt DESC
    """)
    fun getAllPendingThrowItemsFlow(): Flow<List<ItemWithAsset>>

    @Query("""
        SELECT session_items.id, session_items.sessionId, session_items.assetId, session_items.position,
               session_items.decision, session_items.decidedAt,
               media_assets.sourceId, media_assets.uriString, media_assets.displayName, media_assets.mimeType,
               media_assets.isVideo, media_assets.durationMs, media_assets.sizeBytes, media_assets.dateAdded,
               media_assets.width, media_assets.height, media_assets.isFavorite, media_assets.isScreenshot,
               media_assets.albumName
        FROM session_items
        INNER JOIN media_assets ON session_items.assetId = media_assets.id
        WHERE session_items.sessionId = :sessionId AND session_items.decision = 'THROW'
        ORDER BY session_items.decidedAt DESC
    """)
    fun getPendingThrowItemsForSessionFlow(sessionId: String): Flow<List<ItemWithAsset>>

    // --- Decision History for Undo ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecisionHistory(history: DecisionHistoryEntity)

    @Query("SELECT * FROM decision_history WHERE sessionId = :sessionId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestDecisionHistory(sessionId: String): DecisionHistoryEntity?

    @Query("DELETE FROM decision_history WHERE id = :id")
    suspend fun deleteDecisionHistory(id: Long)

    @Query("SELECT decision FROM session_items WHERE sessionId = :sessionId AND assetId = :assetId")
    suspend fun getItemDecision(sessionId: String, assetId: String): String?

    @Transaction
    suspend fun recordDecisionTransaction(
        sessionId: String,
        assetId: String,
        previousDecision: String,
        newDecision: String,
        now: Long
    ) {
        updateItemDecision(sessionId, assetId, newDecision, now)
        insertDecisionHistory(
            DecisionHistoryEntity(
                sessionId = sessionId,
                assetId = assetId,
                previousDecision = previousDecision,
                newDecision = newDecision,
                timestamp = now
            )
        )
    }

    @Transaction
    suspend fun undoLastDecisionTransaction(sessionId: String, now: Long): DecisionHistoryEntity? {
        val lastEvent = getLatestDecisionHistory(sessionId) ?: return null
        updateItemDecision(
            sessionId,
            lastEvent.assetId,
            lastEvent.previousDecision,
            now
        )
        deleteDecisionHistory(lastEvent.id)
        return lastEvent
    }

    // --- Removal Batches ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: RemovalBatchEntity)

    @Query("SELECT * FROM removal_batches ORDER BY timestamp DESC")
    fun getAllBatchesFlow(): Flow<List<RemovalBatchEntity>>

    // --- Aggregates ---
    @Query("SELECT COUNT(DISTINCT assetId) FROM session_items WHERE decision IN ('KEEP', 'THROW')")
    fun getTotalUniqueReviewedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT assetId) FROM session_items WHERE decision = 'KEEP'")
    fun getTotalKeptCountFlow(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT assetId) FROM session_items WHERE decision = 'THROW'")
    fun getTotalPendingThrowCountFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(bytesFreed), 0) FROM removal_batches")
    fun getTotalBytesFreedFlow(): Flow<Long>

    @Query("DELETE FROM session_items WHERE sessionId = :sessionId")
    suspend fun deleteSessionItems(sessionId: String)

    @Query("DELETE FROM decision_history WHERE sessionId = :sessionId")
    suspend fun deleteDecisionHistoryForSession(sessionId: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM session_items")
    suspend fun clearAllSessionItems()

    @Query("DELETE FROM decision_history")
    suspend fun clearAllDecisionHistory()

    @Query("DELETE FROM removal_batches")
    suspend fun clearAllBatches()
}
