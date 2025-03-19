package ru.walkAndTalk.ui.screens.auth.login

data class LoginViewState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
