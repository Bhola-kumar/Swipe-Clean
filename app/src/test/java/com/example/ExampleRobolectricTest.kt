package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.DecisionType
import com.example.data.FilterType
import com.example.data.MediaAssetEntity
import com.example.data.MediaRepository
import com.example.data.MediaSourceInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)
        assertNotNull(context.packageName)
    }

    @Test
    fun `test local storage persistence and undo workflow in Room`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = MediaRepository(context, db.mediaDao())

        val testAssets = listOf(
            MediaAssetEntity(
                id = "test_img_1",
                sourceId = MediaSourceInfo.DeviceLibrary.id,
                uriString = "content://media/external/images/media/101",
                displayName = "IMG_20261026_Test1.jpg",
                mimeType = "image/jpeg",
                isVideo = false,
                sizeBytes = 3_450_000L,
                dateAdded = System.currentTimeMillis() / 1000,
                width = 3000,
                height = 4000,
                albumName = "Camera"
            ),
            MediaAssetEntity(
                id = "test_img_2",
                sourceId = MediaSourceInfo.DeviceLibrary.id,
                uriString = "content://media/external/images/media/102",
                displayName = "Screenshot_20261026_Test2.png",
                mimeType = "image/png",
                isVideo = false,
                sizeBytes = 1_200_000L,
                dateAdded = System.currentTimeMillis() / 1000,
                width = 1080,
                height = 2400,
                isScreenshot = true,
                albumName = "Screenshots"
            )
        )

        val sessionId = repo.startNewSession(
            sourceId = MediaSourceInfo.DeviceLibrary.id,
            title = "Test Local Storage Session",
            filterType = FilterType.ALL,
            assets = testAssets
        )
        assertNotNull(sessionId)

        // Storing decisions into Room local database
        repo.recordDecision(sessionId, testAssets[0].id, DecisionType.KEEP)
        repo.recordDecision(sessionId, testAssets[1].id, DecisionType.THROW)

        val pendingThrowList = repo.allPendingThrowFlow.first()
        assertTrue(pendingThrowList.any { it.assetId == testAssets[1].id })

        // Undo in local database
        val undoSuccess = repo.undoLastDecision(sessionId)
        assertTrue(undoSuccess)
    }

    @Test
    fun `test multiple sessions can be created and paused concurrently`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = MediaRepository(context, db.mediaDao())

        val session1 = repo.startNewSession(
            sourceId = MediaSourceInfo.DeviceLibrary.id,
            title = "Device Camera Session",
            filterType = FilterType.ALL,
            assets = listOf(
                MediaAssetEntity(
                    id = "s1_asset_1",
                    sourceId = MediaSourceInfo.DeviceLibrary.id,
                    uriString = "content://media/external/images/media/201",
                    displayName = "IMG_001.jpg",
                    mimeType = "image/jpeg",
                    isVideo = false,
                    sizeBytes = 2_000_000L,
                    dateAdded = 1000L,
                    width = 1920,
                    height = 1080,
                    albumName = "Camera"
                )
            )
        )

        // Make decision on session 1
        repo.recordDecision(session1, "s1_asset_1", DecisionType.KEEP)

        // Start session 2 from a different source (e.g. system picker) without affecting session 1
        val session2 = repo.startNewSession(
            sourceId = MediaSourceInfo.SystemPicker.id,
            title = "Picker Session",
            filterType = FilterType.ALL,
            assets = listOf(
                MediaAssetEntity(
                    id = "s2_asset_1",
                    sourceId = MediaSourceInfo.SystemPicker.id,
                    uriString = "content://media/external/images/media/301",
                    displayName = "IMG_301.jpg",
                    mimeType = "image/jpeg",
                    isVideo = false,
                    sizeBytes = 1_500_000L,
                    dateAdded = 2000L,
                    width = 1920,
                    height = 1080,
                    albumName = "Picker"
                )
            )
        )

        val allSessions = repo.allSessionsFlow.first()
        assertTrue(allSessions.any { it.id == session1 })
        assertTrue(allSessions.any { it.id == session2 })

        // Test destroying/deleting session 1 cleanly
        repo.deleteSession(session1)
        val sessionsAfterDelete = repo.allSessionsFlow.first()
        assertTrue(sessionsAfterDelete.none { it.id == session1 })
        assertTrue(sessionsAfterDelete.any { it.id == session2 })
    }
}

