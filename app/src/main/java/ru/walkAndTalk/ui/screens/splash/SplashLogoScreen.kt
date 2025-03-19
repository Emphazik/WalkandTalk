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
import ru.walkAndTalk.R
import ru.walkAndTalk.ui.screens.Screens

val montserrat = FontFamily(
    Font(R.font.montserrat, FontWeight.Normal),
    Font(R.font.montserrat_black, FontWeight.Bold),
    Font(R.font.montserrat_medium, FontWeight.Medium),
)

@Composable
fun SplashLogoScreen(navController: NavHostController, context: Context) {
    var isVisible by remember { mutableStateOf(false) }
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_complete", false)

    LaunchedEffect(Unit) {
        isVisible = true
        delay(3500)
        if (isOnboardingCompleted) {
            navController.navigate(Screens.AUTH) {
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
            fontFamily = montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground,
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


//@Composable
//fun SplashLogoScreen(modifier: Modifier = Modifier, onSplashEnded: () -> Unit) {
//    Box(
//        modifier = modifier
//            .size(256.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val canvasWidth = size.width
//            val canvasHeight = size.height
//            val circleRadius = canvasWidth / 8
//            val circle1Center = Offset(canvasWidth / 3, canvasHeight / 2)
//            val circle2Center = Offset(canvasWidth * 2 / 3, canvasHeight / 2)
//            val lineStart = Offset(circle1Center.x + circleRadius, circle1Center.y)
//            val lineEnd = Offset(circle2Center.x - circleRadius, circle2Center.y)
//
//            drawCircle(
//                color = Primary,
//                center = circle1Center,
//                radius = circleRadius
//            )
//            drawCircle(
//                color = Secondary,
//                center = circle2Center,
//                radius = circleRadius
//            )
//            drawLine(
//                color = Color.Black,
//                start = lineStart,
//                end = lineEnd,
//                strokeWidth = 5f,
//            )
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = 150.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Walk",
//                style = TextStyle(
//                    color = Color.Black,
//                    fontSize = 30.sp,
//                    fontWeight = FontWeight.Bold
//                ),
//                textAlign = TextAlign.Center
//            )
//            Text(
//                text = "&",
//                style = TextStyle(
//                    color = Color.Black,
//                    fontSize = 30.sp,
//                    fontWeight = FontWeight.Bold
//                ),
//                textAlign = TextAlign.Center
//            )
//            Text(
//                text = "Talk",
//                style = TextStyle(
//                    color = Color.Black,
//                    fontSize = 30.sp,
////                    fontFamily = "@font/montserrat",
//                    fontWeight = FontWeight.Bold
//                ),
//                textAlign = TextAlign.Center
//            )
//        }
////        onSplashEnded()
//    }
//}



//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Text
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.graphicsLayer
//
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//import ru.walkAndTalk.ui.theme.DarkBlue
//import ru.walkAndTalk.ui.theme.DarkGray
//import ru.walkAndTalk.ui.theme.OnBackground
//import ru.walkAndTalk.ui.theme.Primary
//import ru.walkAndTalk.ui.theme.Secondary


//@Composable
//fun SplashScreen(onSplashEnded: () -> Unit) {
//    val alpha = remember { Animatable(0f) }
//    val scale = remember { Animatable(0.5f) }
//
//    LaunchedEffect(key1 = true) {
//        alpha.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
//        )
//        scale.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
//        )
//        delay(2000)
//        alpha.animateTo(
//            targetValue = 0f,
//            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
//        )
//        onSplashEnded()
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        DarkBlue,
//                        DarkGray
//                    )
//                )
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = "Walk&Talk",
//            style = TextStyle(
//                fontSize = 48.sp,
//                fontWeight = FontWeight.Bold,
//                brush = Brush.linearGradient(
//                    colors = listOf(
//                        Primary,
//                        Secondary
//                    )
//                )
//            ),
//            modifier = Modifier
//                .alpha(alpha.value)
//                .graphicsLayer {
//                    scaleX = scale.value
//                    scaleY = scale.value
//                },
//            color = OnBackground
//        )
//    }
//}