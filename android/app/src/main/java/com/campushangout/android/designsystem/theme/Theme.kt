package com.campushangout.android.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 브랜드 색을 일관되게 유지하려고 Android 12+ 동적 색상(dynamic color)은 쓰지 않는다.
// surfaceContainer 계열을 지정하지 않으면 Material 기본(보라빛) 색이 들어가므로 모두 명시한다.
private val LightColors = lightColorScheme(
    primary = Blue500,
    onPrimary = Gray0,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue900,
    secondary = Coral500,
    onSecondary = Gray0,
    secondaryContainer = Coral50,
    onSecondaryContainer = Coral900,
    tertiary = Blue500,
    onTertiary = Gray0,
    tertiaryContainer = Blue50,
    onTertiaryContainer = Blue900,
    background = Gray50,
    onBackground = Gray900,
    surface = Gray0,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    surfaceDim = Gray100,
    surfaceBright = Gray0,
    surfaceContainerLowest = Gray0,
    surfaceContainerLow = Gray50,
    surfaceContainer = Gray0,
    surfaceContainerHigh = Gray50,
    surfaceContainerHighest = Gray100,
    outline = Gray300,
    outlineVariant = Gray100,
    error = Red500,
    onError = Gray0,
)

private val DarkColors = darkColorScheme(
    primary = Blue200,
    onPrimary = Blue900,
    primaryContainer = Blue900,
    onPrimaryContainer = Blue50,
    secondary = Coral200,
    onSecondary = Coral900,
    secondaryContainer = Coral900,
    onSecondaryContainer = Coral50,
    tertiary = Blue200,
    onTertiary = Blue900,
    tertiaryContainer = Blue900,
    onTertiaryContainer = Blue50,
    background = Gray950,
    onBackground = Gray100,
    surface = Gray900,
    onSurface = Gray100,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    surfaceDim = Gray950,
    surfaceBright = Gray800,
    surfaceContainerLowest = Gray950,
    surfaceContainerLow = Gray900,
    surfaceContainer = Gray900,
    surfaceContainerHigh = Gray800,
    surfaceContainerHighest = Gray700,
    outline = Gray500,
    outlineVariant = Gray800,
    error = Red200,
    onError = Gray950,
)

@Composable
fun CampusHangoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CampusHangoutTypography,
        shapes = CampusHangoutShapes,
        content = content,
    )
}
