package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class SwipeDirectionConfig {
    NORMAL, // Right = Keep, Left = Throw
    INVERTED // Right = Throw, Left = Keep
}

enum class MediaSortOrder(val label: String) {
    NEWEST_FIRST("Newest First (Chronological)"),
    OLDEST_FIRST("Oldest First"),
    LARGEST_FIRST("Largest Items First"),
    SHUFFLE("Shuffle (Random)")
}

data class AppSettingsState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val swipeDirection: SwipeDirectionConfig = SwipeDirectionConfig.NORMAL,
    val sortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val autoplayVideos: Boolean = true,
    val muteVideosByDefault: Boolean = true,
    val protectFavorites: Boolean = true,
    val leftHandedMode: Boolean = false,
    val onboardingCompleted: Boolean = false
)

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("keep_or_throw_prefs", Context.MODE_PRIVATE)

    fun getSettings(): AppSettingsState {
        return AppSettingsState(
            themeMode = AppThemeMode.valueOf(prefs.getString("themeMode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name),
            swipeDirection = SwipeDirectionConfig.valueOf(prefs.getString("swipeDir", SwipeDirectionConfig.NORMAL.name) ?: SwipeDirectionConfig.NORMAL.name),
            sortOrder = MediaSortOrder.valueOf(prefs.getString("sortOrder", MediaSortOrder.NEWEST_FIRST.name) ?: MediaSortOrder.NEWEST_FIRST.name),
            hapticsEnabled = prefs.getBoolean("haptics", true),
            soundEnabled = prefs.getBoolean("sound", false),
            autoplayVideos = prefs.getBoolean("autoplayVideos", true),
            muteVideosByDefault = prefs.getBoolean("muteVideos", true),
            protectFavorites = prefs.getBoolean("protectFavorites", true),
            leftHandedMode = prefs.getBoolean("leftHanded", false),
            onboardingCompleted = prefs.getBoolean("onboardingDone", false)
        )
    }

    fun updateSettings(settings: AppSettingsState) {
        prefs.edit()
            .putString("themeMode", settings.themeMode.name)
            .putString("swipeDir", settings.swipeDirection.name)
            .putString("sortOrder", settings.sortOrder.name)
            .putBoolean("haptics", settings.hapticsEnabled)
            .putBoolean("sound", settings.soundEnabled)
            .putBoolean("autoplayVideos", settings.autoplayVideos)
            .putBoolean("muteVideos", settings.muteVideosByDefault)
            .putBoolean("protectFavorites", settings.protectFavorites)
            .putBoolean("leftHanded", settings.leftHandedMode)
            .putBoolean("onboardingDone", settings.onboardingCompleted)
            .apply()
    }
}
