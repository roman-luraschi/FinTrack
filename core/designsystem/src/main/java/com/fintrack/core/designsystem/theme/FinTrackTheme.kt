package com.fintrack.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF1B5E20)
private val PrimaryDark = Color(0xFF81C784)
private val Secondary = Color(0xFF00695C)
private val ExpenseColor = Color(0xFFC62828)
private val IncomeColor = Color(0xFF2E7D32)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Color(0xFF558B2F),
    error = ExpenseColor,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFF9CCC65),
    error = Color(0xFFEF5350),
)

object FinTrackColors {
    val expense = ExpenseColor
    val income = IncomeColor
}

@Composable
fun FinTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinTrackTypography,
        content = content,
    )
}
