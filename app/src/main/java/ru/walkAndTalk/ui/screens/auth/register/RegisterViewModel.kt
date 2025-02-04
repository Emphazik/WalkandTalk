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

    }

}