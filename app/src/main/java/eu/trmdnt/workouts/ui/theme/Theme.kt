package eu.trmdnt.workouts.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import eu.trmdnt.workouts.ui.settings.Theme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

fun Color.darken(factor: Float): Color {
    val clampedFactor = factor.coerceIn(0f, 1f)
    return Color(
        red = this.red * clampedFactor,
        green = this.green * clampedFactor,
        blue = this.blue * clampedFactor,
        alpha = this.alpha // Preserve original alpha
    )
}

@Composable
fun AppTheme(

    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    theme: Theme,
    content: @Composable () -> Unit
) {
    val isDark = isDarkMode(theme)

    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    if (theme == Theme.Oled) {
        val darkenFactor = 0.5f
        colorScheme = colorScheme.copy(
            //primary = colorScheme.primary.darken(darkenFactor),
            //onPrimary = colorScheme.onPrimary.darken(darkenFactor),
            primaryContainer = colorScheme.primaryContainer.darken(darkenFactor),
            //onPrimaryContainer = colorScheme.onPrimaryContainer.darken(darkenFactor),
            //secondary = colorScheme.secondary.darken(darkenFactor),
            //onSecondary = colorScheme.onSecondary.darken(darkenFactor),
            secondaryContainer = colorScheme.secondaryContainer.darken(darkenFactor),
            //onSecondaryContainer = colorScheme.onSecondaryContainer.darken(darkenFactor),
            //tertiary = colorScheme.tertiary.darken(darkenFactor),
            //onTertiary = colorScheme.onTertiary.darken(darkenFactor),
            tertiaryContainer = colorScheme.tertiaryContainer.darken(darkenFactor),
            //onTertiaryContainer = colorScheme.onTertiaryContainer.darken(darkenFactor),
            //error = colorScheme.error.darken(darkenFactor),
            //onError = colorScheme.onError.darken(darkenFactor),
            //errorContainer = colorScheme.errorContainer.darken(darkenFactor),
            //onErrorContainer = colorScheme.onErrorContainer.darken(darkenFactor),
            background = colorScheme.background.darken(darkenFactor),
            //onBackground = colorScheme.onBackground.darken(darkenFactor),
            surface = colorScheme.surface.darken(darkenFactor),
            //onSurface = colorScheme.onSurface.darken(darkenFactor),
            surfaceVariant = colorScheme.surfaceVariant.darken(darkenFactor),
            //onSurfaceVariant = colorScheme.onSurfaceVariant.darken(darkenFactor),
            outline = colorScheme.outline.darken(darkenFactor),
            outlineVariant = colorScheme.outlineVariant.darken(darkenFactor),
            //scrim = colorScheme.scrim.darken(darkenFactor),
            //inverseSurface = colorScheme.inverseSurface.darken(darkenFactor),
            //inverseOnSurface = colorScheme.inverseOnSurface.darken(darkenFactor),
            //inversePrimary = colorScheme.inversePrimary.darken(darkenFactor),
            //surfaceDim = colorScheme.surfaceDim.darken(darkenFactor),
            //surfaceBright = colorScheme.surfaceBright.darken(darkenFactor),
            surfaceContainerLowest = colorScheme.surfaceContainerLowest.darken(darkenFactor),
            surfaceContainerLow = colorScheme.surfaceContainerLow.darken(darkenFactor),
            surfaceContainer = colorScheme.surfaceContainer.darken(darkenFactor),
            surfaceContainerHigh = colorScheme.surfaceContainerHigh.darken(darkenFactor),
            surfaceContainerHighest = colorScheme.surfaceContainerHighest.darken(darkenFactor),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun isDarkMode(theme: Theme) = when (theme) {
    Theme.System -> isSystemInDarkTheme()
    Theme.Light -> false
    Theme.Dark -> true
    Theme.Oled -> true
}