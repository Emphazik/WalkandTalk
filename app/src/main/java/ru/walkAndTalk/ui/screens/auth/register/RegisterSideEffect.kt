package ru.walkAndTalk.ui.screens.auth.register

sealed interface RegisterSideEffect {
    data object OnNavigateLogin : RegisterSideEffect
    data class OnNavigateMain(val id: String) : RegisterSideEffect
}