package ru.walkAndTalk.ui.screens.auth.register

import android.net.Uri

data class RegisterViewState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
