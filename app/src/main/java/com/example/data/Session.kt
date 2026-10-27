package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DecisionType {
    NONE,
    KEEP,
    THROW,
    SKIP
}

enum class FilterType {
    ALL,
    PHOTOS_ONLY,
    VIDEOS_ONLY,
    SCREENSHOTS,
    LARGE_ITEMS
}

enum class BatchStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    PARTIAL,
    CANCELED
}

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val title: String,
    val filterType: String = FilterType.ALL.name,
    val totalCount: Int = 0,
    val reviewedCount: Int = 0,
    val keptCount: Int = 0,
    val throwCount: Int = 0,
    val skippedCount: Int = 0,
    val estimatedPendingBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivityAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "session_items",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["assetId"]),
        Index(value = ["sessionId", "position"])
    ]
)
data class SessionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val assetId: String,
    val position: Int,
    val decision: String = DecisionType.NONE.name,
    val decidedAt: Long = 0L
)

@Entity(
    tableName = "decision_history",
    indices = [Index(value = ["sessionId"])]
)
data class DecisionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val assetId: String,
    val previousDecision: String,
    val newDecision: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "removal_batches")
data class RemovalBatchEntity(
    @PrimaryKey val id: String,
    val sessionId: String?,
    val sourceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val requestedCount: Int,
    val succeededCount: Int,
    val failedCount: Int,
    val canceledCount: Int,
    val bytesFreed: Long,
    val status: String = BatchStatus.SUCCEEDED.name,
    val note: String = ""
)
