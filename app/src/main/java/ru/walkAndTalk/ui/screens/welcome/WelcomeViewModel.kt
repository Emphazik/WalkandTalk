package ru.walkAndTalk.ui.screens.welcome

import ru.walkAndTalk.ui.orbit.ContainerViewModel

class WelcomeViewModel : ContainerViewModel<Unit, WelcomeSideEffect>(Unit) {

    fun onLoginClick() = intent {
        postSideEffect(WelcomeSideEffect.OnNavigateLogin)
    }

    fun onRegisterClick() = intent {
        postSideEffect(WelcomeSideEffect.OnNavigateRegister)
    }

}