package ru.walkAndTalk.ui.screens.auth.login

import ru.walkAndTalk.ui.Mvi

class LoginViewModel(

) : Mvi<LoginViewState, LoginSideEffect>(
    initialState = LoginViewState()
) {

    fun onUsernameChanged(username: String) = blockingIntent {
        reduce { state.copy(username = username) }
    }

    fun onPasswordChanged(password: String) = blockingIntent {
        reduce { state.copy(password = password) }
    }

    fun onLoginClick() = intent {
        // login here
    }

    fun onRegisterClick() = intent {
        postSideEffect(LoginSideEffect.OnRegisterClick)
    }

}
