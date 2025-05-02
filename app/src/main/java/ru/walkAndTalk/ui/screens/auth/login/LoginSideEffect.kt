package ru.walkAndTalk.ui.screens.auth.login

sealed interface LoginSideEffect {
    data object OnNavigateRegister : LoginSideEffect
    data object OnNavigateMain : LoginSideEffect
}