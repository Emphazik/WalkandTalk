package ru.walkAndTalk.ui.screens.auth.login

sealed interface LoginSideEffect {
    data object OnRegisterClick : LoginSideEffect

}