package com.quenan.duji.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.quenan.duji.data.settings.COLOR_MODE_DARK
import com.quenan.duji.data.settings.COLOR_MODE_LIGHT
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

val LocalEnableBlur = staticCompositionLocalOf { false }

@Composable
fun DuJiTheme(
    colorModeIndex: Int = 0,
    enableBlur: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorSchemeMode = when (colorModeIndex) {
        COLOR_MODE_LIGHT -> ColorSchemeMode.Light
        COLOR_MODE_DARK -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }
    val controller = remember(colorSchemeMode) { ThemeController(colorSchemeMode) }
    val isDark = when (colorSchemeMode) {
        ColorSchemeMode.Dark -> true
        ColorSchemeMode.Light -> false
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current

    MiuixTheme(
        controller = controller,
        content = {
            SideEffect {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            }
            CompositionLocalProvider(
                LocalEnableBlur provides enableBlur,
                content = content
            )
        }
    )
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(): Boolean = isSystemInDarkTheme()
