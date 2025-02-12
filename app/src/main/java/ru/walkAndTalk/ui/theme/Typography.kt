package ru.walkAndTalk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.walkAndTalk.ui.screens.splash.montserrat

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    displayMedium = TextStyle(
        fontFamily = montserrat,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = montserrat,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = montserrat,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)
