package ru.walkAndTalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//private val LightColorScheme = lightColorScheme(
//    primary = Primary,
//    secondary = Secondary,
//    onPrimary = OnPrimary,
//    onBackground = DarkGray,
//    background = Background
//)
//
//private val DarkColorScheme = darkColorScheme(
//    primary = Primary,
//    secondary = Secondary,
//    onPrimary = OnPrimary,
//    onBackground = OnBackground,
//    background = DarkGray
//)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = OnPrimary,
    onSecondary = Color.White,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimary,
    onSecondary = Color.Black,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark
)

@Composable
fun WalkTalkTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

//Varik 3

