package ru.walkAndTalk.ui.screens.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.walkAndTalk.R

val montserratFont = FontFamily(Font(R.font.montserrat_semi_bold))

@Composable
fun OnboardingScreen(navController: NavHostController, context: Context) {
    val pagerState = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    val pages = listOf(
        OnboardingPage(R.drawable.ic_date, "Найди новых друзей", "Смахни вправо и знакомься с интересными людьми"),
        OnboardingPage(R.drawable.ic_chat, "Общайся без границ", "Переписывайся с теми, кто тебе понравился"),
        OnboardingPage(R.drawable.ic_photo, "Делись моментами", "Отправляй фото и делай общение живым"),
        OnboardingPage(R.drawable.ic_notify, "Всегда на связи", "Не пропусти новые знакомства и сообщения")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState) { page ->
            OnboardingPageContent(pages[page])
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage < pages.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            sharedPreferences.edit().putBoolean("onboarding_complete", true).apply()
                            navController.navigate("welcome") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier.size(256.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.title,
                fontFamily = montserratFont,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = page.description,
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}


data class OnboardingPage(val imageRes: Int, val title: String, val description: String)
