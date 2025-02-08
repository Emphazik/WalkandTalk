package ru.walkAndTalk.ui.screens.auth.register

import ru.walkAndTalk.ui.Mvi

class RegisterViewModel(
    // registerUseCase
) : Mvi<RegisterViewState, RegisterSideEffect>(
    initialState = RegisterViewState()
) {

    fun onLoginClick() = intent {
        postSideEffect(RegisterSideEffect.OnLoginClick)
    }

    fun onRegisterClick() = intent {
        // Handle register logic here
    }

    fun onLoginChanged(newLogin: String) = intent {
        // Handle the change in login here
        println("Login changed to: $newLogin")
        reduce { state.copy(login = newLogin) }
    }

    fun onPasswordChanged(newPassword: String) = intent {
        // Handle the change in password here
        println("Password changed to: $newPassword")
        reduce { state.copy(password = newPassword) }
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) = intent {
        // Handle the change in confirm password here
        println("Confirm password changed to: $newConfirmPassword")
        reduce { state.copy(confirmPassword = newConfirmPassword) }
    }
}