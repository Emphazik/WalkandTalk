package ru.walkAndTalk.ui.screens.auth.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegisterViewState())
    val state: StateFlow<RegisterViewState> = _state

    fun onNameChange(newName: String) {
        _state.value = _state.value.copy(name = newName)
    }

    fun onPhoneChange(newPhone: String) {
        _state.value = _state.value.copy(phone = newPhone)
    }

    fun onEmailChange(newEmail: String) {
        _state.value = _state.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(password = newPassword)
    }

    fun onProfileImageSelected(uri: Uri) {
        _state.value = _state.value.copy(profileImageUri = uri)
    }

    fun onRegisterClick() {
        TODO("Not yet implemented")
    }
}

