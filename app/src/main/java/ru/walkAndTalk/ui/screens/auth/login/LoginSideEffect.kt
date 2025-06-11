package ru.walkAndTalk.ui.screens.auth.login

sealed interface LoginSideEffect {
    data object OnNavigateRegister : LoginSideEffect
    data class OnNavigateAdmin(val userId: String) : LoginSideEffect
    data class OnNavigateMain(val id: String) : LoginSideEffect
}