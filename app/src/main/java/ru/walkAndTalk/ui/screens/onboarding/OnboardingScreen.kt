package ru.walkAndTalk.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.walkAndTalk.R

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onNavigateWelcome: () -> Unit,
) {
    val state by viewModel.collectAsState()

    val pages = listOf(
        OnboardingPage(R.drawable.ic_date1, "Найди новых друзей", "Смахни вправо и знакомься с интересными людьми"),
        OnboardingPage(R.drawable.ic_chat, "Общайся без границ", "Переписывайся с теми, кто тебе понравился"),
        OnboardingPage(R.drawable.ic_photo1, "Делись моментами", "Отправляй фото и делай общение живым"),
        OnboardingPage(R.drawable.ic_notify1, "Всегда на связи", "Не пропусти новые знакомства и сообщения")
    )
    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.pageCount }
    )

    viewModel.collectSideEffect {
        when (it) {
            is OnboardingSideEffect.OnNextClick -> pagerState.animateScrollToPage(it.index)
            is OnboardingSideEffect.OnNavigateWelcome -> onNavigateWelcome()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    viewModel.onNextClick()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onStateChange(0, pages.size)
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
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = page.description,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 3,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)
