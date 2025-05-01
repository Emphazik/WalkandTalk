package ru.walkAndTalk.ui.screens.splash

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.Screens

@Serializable
object SplashLogoScreen

@Composable
fun SplashLogoScreen(navController: NavHostController, context: Context) {
    var isVisible by remember { mutableStateOf(false) }
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_complete", false)
    println("Splash: isOnboardingCompleted = $isOnboardingCompleted")
    LaunchedEffect(Unit) {
        isVisible = true
        delay(3500)
        if (isOnboardingCompleted) {
            navController.navigate(Screens.WELCOME) {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate(Screens.ONBOARDING) {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alphaAnimation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutExpo),
        label = "scaleAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color.DarkGray else Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoaderAnimation(
            modifier = Modifier.size(400.dp),
            anim = R.raw.animation4
        )
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = "Walk & Talk",
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
            modifier = Modifier
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
        )
    }
}

@Composable
fun LoaderAnimation(modifier: Modifier, anim: Int) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(anim))

    LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever,
        modifier = modifier )
}