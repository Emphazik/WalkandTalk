package ru.walkAndTalk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.walkAndTalk.R

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    displayMedium = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = montserratFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)