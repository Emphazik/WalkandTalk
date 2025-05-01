package ru.walkAndTalk.ui.screens.auth.login

sealed interface LoginSideEffect {
    data object OnRegisterClick : LoginSideEffect
    data object NavigateToMainScreen : LoginSideEffect
    data class ShowError(val message: String) : LoginSideEffect
}