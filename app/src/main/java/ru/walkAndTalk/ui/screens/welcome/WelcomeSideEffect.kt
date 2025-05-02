package ru.walkAndTalk.ui.screens.welcome

sealed interface WelcomeSideEffect {
    data object OnNavigateLogin : WelcomeSideEffect
    data object OnNavigateRegister : WelcomeSideEffect
}