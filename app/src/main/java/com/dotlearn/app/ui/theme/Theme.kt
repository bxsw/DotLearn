package com.dotlearn.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF534AB7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4E1FF),
    onPrimaryContainer = Color(0xFF1B1149),
    secondary = Color(0xFF0F6E56),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB9EEDC),
    onSecondaryContainer = Color(0xFF00251A),
    tertiary = Color(0xFF8F4C38),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBD1),
    onTertiaryContainer = Color(0xFF3A0B01),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE6E1EC),
    onSurfaceVariant = Color(0xFF48454E),
    outline = Color(0xFF79757F),
    outlineVariant = Color(0xFFCAC4CF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC5C0FF),
    onPrimary = Color(0xFF26215C),
    primaryContainer = Color(0xFF3C3489),
    onPrimaryContainer = Color(0xFFE4E1FF),
    secondary = Color(0xFF84D5BC),
    onSecondary = Color(0xFF003828),
    secondaryContainer = Color(0xFF085041),
    onSecondaryContainer = Color(0xFFB9EEDC),
    tertiary = Color(0xFFFFB59E),
    onTertiary = Color(0xFF55210E),
    tertiaryContainer = Color(0xFF712B13),
    onTertiaryContainer = Color(0xFFFFDBD1),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF48454E),
    onSurfaceVariant = Color(0xFFCAC4CF),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF48454E)
)

/** 主题模式：0 跟随系统 / 1 浅色 / 2 深色 */
@Composable
fun DotLearnTheme(
    themeMode: Int = 0,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}

/** 学科配色候选。 */
val SubjectPalette: List<Color> = listOf(
    Color(0xFF534AB7),
    Color(0xFF0F6E56),
    Color(0xFF185FA5),
    Color(0xFF854F0B),
    Color(0xFF993556),
    Color(0xFF3B6D11),
    Color(0xFFD85A30),
    Color(0xFF5F5E5A)
)
