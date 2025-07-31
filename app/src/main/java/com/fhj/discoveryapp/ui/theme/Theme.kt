package com.fhj.discoveryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.fhj.discoveryapp.chat.ChatComposeScreen

@Composable
fun DiscoveryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(AppColors.DarkPrimary),
            onPrimary = Color(AppColors.DarkOnPrimary),
            secondary = Color(AppColors.DarkSecondary),
            onSecondary = Color(AppColors.DarkOnSecondary),
            background = Color(AppColors.DarkBackground),
            onBackground = Color(AppColors.DarkOnBackground),
            surface = Color(AppColors.DarkSurface),
            onSurface = Color(AppColors.DarkOnSurface),
            error = Color(AppColors.DarkError),
            onError = Color(AppColors.DarkOnBackground),
            // 使用PrimaryDark作为primaryContainer
            primaryContainer = Color(AppColors.DarkPrimaryDark),
            onPrimaryContainer = Color(AppColors.DarkOnPrimary),
            // 使用SecondaryDark作为secondaryContainer
            secondaryContainer = Color(AppColors.DarkSecondaryDark),
            onSecondaryContainer = Color(AppColors.DarkOnSecondary),
        )
    } else {
        lightColorScheme(
            primary = Color(AppColors.Primary),
            onPrimary = Color(AppColors.OnPrimary),
            secondary = Color(AppColors.Secondary),
            onSecondary = Color(AppColors.OnSecondary),
            background = Color(AppColors.Background),
            onBackground = Color(AppColors.OnBackground),
            surface = Color(AppColors.Surface),
            onSurface = Color(AppColors.OnSurface),
            error = Color(AppColors.Error),
            onError = Color(AppColors.OnBackground),
            // 使用PrimaryDark作为primaryContainer
            primaryContainer = Color(AppColors.PrimaryDark),
            onPrimaryContainer = Color(AppColors.OnPrimary),
            // 使用SecondaryDark作为secondaryContainer
            secondaryContainer = Color(AppColors.SecondaryDark),
            onSecondaryContainer = Color(AppColors.OnSecondary),
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}

@Composable
@Preview
fun pre(){
    DiscoveryAppTheme{
        ChatComposeScreen("")
    }
}