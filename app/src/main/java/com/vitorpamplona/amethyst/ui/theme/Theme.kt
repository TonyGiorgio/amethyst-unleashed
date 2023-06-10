package com.vitorpamplona.amethyst.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,
    secondaryVariant = Color(0xFFF7931A)
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200,
    secondaryVariant = Color(0xFFB66605)
)

private val DarkNewItemBackground = DarkColorPalette.primary.copy(0.12f)
private val LightNewItemBackground = LightColorPalette.primary.copy(0.12f)

private val DarkReplyItemBackground = DarkColorPalette.onSurface.copy(alpha = 0.05f)
private val LightReplyItemBackground = LightColorPalette.onSurface.copy(alpha = 0.05f)

private val DarkSelectedNote = DarkNewItemBackground.compositeOver(DarkColorPalette.background)
private val LightSelectedNote = LightNewItemBackground.compositeOver(LightColorPalette.background)

val Colors.newItemBackgroundColor: Color
    get() = if (isLight) LightNewItemBackground else DarkNewItemBackground

val Colors.replyBackground: Color
    get() = if (isLight) LightReplyItemBackground else DarkReplyItemBackground

val Colors.selectedNote: Color
    get() = if (isLight) LightSelectedNote else DarkSelectedNote

@Composable
fun AmethystTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = DarkColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )

    val view = LocalView.current
    if (!view.isInEditMode && darkTheme) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
        }
    }
}
