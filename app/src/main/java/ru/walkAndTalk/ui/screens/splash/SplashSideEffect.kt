package ru.walkAndTalk.ui.screens.splash

sealed interface SplashSideEffect {
    data object OnNavigateMain : SplashSideEffect
    data object OnNavigateWelcome : SplashSideEffect
    data object OnNavigateOnboarding : SplashSideEffect
}