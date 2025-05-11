package ru.walkAndTalk.ui.screens.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel(),
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit,
    navController: NavHostController,
) {
    viewModel.collectSideEffect {
        when (it) {
            is WelcomeSideEffect.OnNavigateLogin -> onNavigateLogin()
            is WelcomeSideEffect.OnNavigateRegister -> onNavigateRegister()
        }
    }

    val logoScale by remember { mutableFloatStateOf(0.8f) }
    val scaleAnim = animateFloatAsState(
        targetValue = logoScale,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    val fadeAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fadeAnim.animateTo(1f, animationSpec = tween(1000))
    }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.blue_abstractback),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(fadeAnim.value),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.walktalk_logotype),
                contentDescription = "WalkTalk Logo",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scaleAnim.value)
                    .offset { IntOffset(x = 0, y = animatedOffset.dp.roundToPx()) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Добро пожаловать в W & T!",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF00796B),
                modifier = Modifier.alpha(fadeAnim.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Общайтесь и знакомьтесь с интересными людьми из вашего города. Планируйте встречи и отправляйтесь на прогулки.",
                fontSize = 16.sp,
                color = Color(0xFF004D40),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .alpha(fadeAnim.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedButton(
                text = "Войти",
                color = Color(0xFF00897B),
                onClick = { viewModel.onLoginClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedButton(
                text = "Создать",
                color = Color(0xFF004D40),
                onClick = { viewModel.onRegisterClick() }
            )
        }
    }
}

@Composable
fun AnimatedButton(text: String, color: Color, onClick: () -> Unit) {
    val pressAnim = remember { Animatable(1f) }
    val hoverAnim = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            coroutineScope.launch {
                pressAnim.animateTo(0.9f, animationSpec = tween(100))
                pressAnim.animateTo(1f, animationSpec = tween(100))
                onClick()
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = hoverAnim.value)),
        modifier = Modifier
            .width(200.dp)
            .height(50.dp)
            .scale(pressAnim.value)
    ) {
        Text(text, fontSize = 18.sp, color = Color.White)
    }
}


//@Composable
//fun WelcomeScreen(navController: NavHostController) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.blue_abstractback),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.walktalk_logotype),
//                contentDescription = "WalkTalk Logo",
//                modifier = Modifier.size(200.dp)
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Text(
//                text = "Добро пожаловать в Walk & Talk!",
//                fontFamily = montserratFont,
//                fontSize = 28.sp,
//                color = Color(0xFF00796B),
//                style = TextStyle(fontFamily = montserratFont)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Match and chat with people you like from your area.",
//                fontFamily = montserratFont,
//                fontSize = 16.sp,
//                color = Color(0xFF004D40),
//                modifier = Modifier.padding(horizontal = 24.dp),
//                style = TextStyle(fontFamily = montserratFont),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Button(
//                onClick = {
//                    Log.d("Navigation", "ПРОВЕРКА LOGIN screen")
//                    navController.navigate("login") },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
//                modifier = Modifier
//                    .width(200.dp)
//                    .height(50.dp)
//            ) {
//                Text("Войти", fontFamily = montserratFont, fontSize = 18.sp, color = Color.White)
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    Log.d("Navigation", "ПРОВЕРКА REG screen")
//                    navController.navigate("register") },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
//                modifier = Modifier
//                    .width(200.dp)
//                    .height(50.dp)
//            ) {
//                Text("Зарегистрироваться", fontFamily = montserratFont, fontSize = 18.sp, color = Color.White)
//            }
//        }
//    }
//}
