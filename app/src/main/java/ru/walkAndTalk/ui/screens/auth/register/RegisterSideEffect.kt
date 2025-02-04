package ru.walkAndTalk.ui.screens.auth.register

sealed interface RegisterSideEffect {
    data object OnLoginClick : RegisterSideEffect
}