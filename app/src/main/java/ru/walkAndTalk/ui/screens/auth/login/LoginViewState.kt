package ru.walkAndTalk.ui.screens.auth.login

import ru.walkAndTalk.data.model.UserDto

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: UserDto? = null // Добавляем для хранения пользователя после авторизации через VK
)
