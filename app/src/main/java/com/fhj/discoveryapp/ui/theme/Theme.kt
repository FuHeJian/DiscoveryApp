package com.fhj.discoveryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.fhj.discoveryapp.chat.ChatComposeScreen

@Composable
fun DiscoveryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = Color(AppColors.Primary),
            primaryVariant = Color(AppColors.PrimaryDark),
            secondary = Color(AppColors.Secondary),
            secondaryVariant = Color(AppColors.SecondaryDark),
            background = Color(AppColors.Background),
            surface = Color(AppColors.Surface),
            onPrimary = Color(AppColors.OnPrimary),
            onSecondary = Color(AppColors.OnSecondary),
            onBackground = Color(AppColors.OnBackground),
            onSurface = Color(AppColors.OnSurface),
            error = Color(AppColors.Error),
        )
    } else {
        lightColors(
            primary = Color(AppColors.Primary),
            primaryVariant = Color(AppColors.PrimaryDark),
            secondary = Color(AppColors.Secondary),
            secondaryVariant = Color(AppColors.SecondaryDark),
            background = Color(AppColors.Background),
            surface = Color(AppColors.Surface),
            onPrimary = Color(AppColors.OnPrimary),
            onSecondary = Color(AppColors.OnSecondary),
            onBackground = Color(AppColors.OnBackground),
            onSurface = Color(AppColors.OnSurface),
            error = Color(AppColors.Error),
        )
    }
    MaterialTheme(
        colors = colors,
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