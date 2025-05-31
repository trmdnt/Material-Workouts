package eu.trmdnt.workouts.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import eu.trmdnt.workouts.settings.Theme

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
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
    dynamicColor: Boolean,
    theme: Theme,
    content: @Composable () -> Unit
) {
    val isDark = isDarkMode(theme)

    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDark -> darkScheme
        else -> lightScheme
    }

    if (theme == Theme.Oled) {
        val darkenFactor = 0.6f
        colorScheme = colorScheme.copy(
            primaryContainer = colorScheme.primaryContainer.darken(darkenFactor),
            secondaryContainer = colorScheme.secondaryContainer.darken(darkenFactor),
            tertiaryContainer = colorScheme.tertiaryContainer.darken(darkenFactor),
            background = Color.Black,
            surface = colorScheme.surface.darken(darkenFactor),
            surfaceVariant = colorScheme.surfaceVariant.darken(darkenFactor),
            outline = colorScheme.outline.darken(darkenFactor),
            outlineVariant = colorScheme.outlineVariant.darken(darkenFactor),
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