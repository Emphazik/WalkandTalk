package ru.walkAndTalk.ui.screens.splash

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onNavigateMain: (String) -> Unit,
    onNavigateWelcome: () -> Unit,
    onNavigateOnboarding: () -> Unit,
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect {
        when (it) {
            is SplashSideEffect.OnNavigateMain -> onNavigateMain(it.id)
            is SplashSideEffect.OnNavigateWelcome -> onNavigateWelcome()
            is SplashSideEffect.OnNavigateOnboarding -> onNavigateOnboarding()
        }
    }

    LaunchedEffect(state.isFirstLaunch) {
        delay(3500)
        viewModel.onIsFirstLaunchChange()
    }

    val alpha by animateFloatAsState(
        targetValue = if (state.isAnimationVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val scale by animateFloatAsState(
        targetValue = if (state.isAnimationVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutExpo)
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
            text = stringResource(R.string.app_name),
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
fun LoaderAnimation(
    modifier: Modifier = Modifier,
    anim: Int
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(anim))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}