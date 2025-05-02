package ru.walkAndTalk.ui.screens.onboarding

sealed interface OnboardingSideEffect {
    data class OnNextClick(val index: Int) : OnboardingSideEffect
    data object OnNavigateWelcome : OnboardingSideEffect
}