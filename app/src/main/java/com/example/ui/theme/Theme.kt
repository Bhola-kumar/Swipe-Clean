package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme with Amber Accent as primary theme
private val AmberLightColorScheme = lightColorScheme(
    primary = Amber600,
    onPrimary = Color.White,
    primaryContainer = Amber100,
    onPrimaryContainer = Amber900,
    secondary = Amber700,
    onSecondary = Color.White,
    secondaryContainer = Amber200,
    onSecondaryContainer = Amber900,
    tertiary = KeepEmeraldDark,
    onTertiary = Color.White,
    tertiaryContainer = KeepContainerLight,
    onTertiaryContainer = KeepOnContainer,
    background = CreamBackground,
    onBackground = TextDarkPrimary,
    surface = PureWhite,
    onSurface = TextDarkPrimary,
    surfaceVariant = StoneVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = StoneBorder,
    outlineVariant = StoneVariant
)

@Composable
fun KeepOrThrowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmberLightColorScheme,
        typography = Typography,
        content = content
    )
}
