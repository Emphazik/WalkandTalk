package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.walkAndTalk.ui.screens.auth.login.LoginViewModel
import ru.walkAndTalk.ui.screens.auth.register.RegisterViewModel
import ru.walkAndTalk.ui.screens.main.MainViewModel
import ru.walkAndTalk.ui.screens.onboarding.OnboardingViewModel
import ru.walkAndTalk.ui.screens.splash.SplashViewModel
import ru.walkAndTalk.ui.screens.welcome.WelcomeViewModel

private val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::WelcomeViewModel)

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)

    viewModelOf(::MainViewModel)
}

internal val presentationModule = module {
    includes(
        viewModelModule
    )
}