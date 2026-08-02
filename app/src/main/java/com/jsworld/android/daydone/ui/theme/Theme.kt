package com.jsworld.android.daydone.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TossBlue,
    onPrimary = White,
    primaryContainer = TossBlueContainer,
    onPrimaryContainer = TossBlueDark,

    secondary = InkStrong,
    onSecondary = White,
    secondaryContainer = BgGray,
    onSecondaryContainer = InkStrong,

    tertiary = TossTeal,
    onTertiary = White,

    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,

    surfaceVariant = BgGray,
    onSurfaceVariant = GrayText,

    surfaceContainerLowest = White,
    surfaceContainerLow = BgGraySoft,
    surfaceContainer = BgGraySoft,
    surfaceContainerHigh = BgGray,
    surfaceContainerHighest = BgGray,

    outline = Line,
    outlineVariant = BgGray,

    error = TossRed,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = TossBlueOnDark,
    onPrimary = White,
    primaryContainer = TossBlueContainerDark,
    onPrimaryContainer = TossBlueContainer,

    secondary = DarkText,
    onSecondary = DarkBg,
    secondaryContainer = DarkCard,
    onSecondaryContainer = DarkText,

    tertiary = TossTeal,
    onTertiary = White,

    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,

    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkGrayText,

    surfaceContainerLowest = DarkBg,
    surfaceContainerLow = DarkCard,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkCardHigh,
    surfaceContainerHighest = DarkCardHigh,

    outline = DarkLine,
    outlineVariant = DarkLine,

    error = TossRed,
    onError = White
)

private val DayDoneShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DayDoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 기본 프로젝트 느낌을 없애기 위해 Dynamic Color(안드로이드 12+ 시스템 색상)를 끈다.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DayDoneShapes,
        content = content
    )
}
