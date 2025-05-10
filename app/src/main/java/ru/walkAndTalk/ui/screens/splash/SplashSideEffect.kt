package ru.walkAndTalk.ui.screens.splash

sealed interface SplashSideEffect {
    data class OnNavigateMain(val id: String) : SplashSideEffect
    data object OnNavigateWelcome : SplashSideEffect
    data object OnNavigateOnboarding : SplashSideEffect
}