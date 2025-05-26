package ru.walkAndTalk.data.di

import io.ktor.http.parameters
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import ru.walkAndTalk.ui.screens.auth.login.LoginViewModel
import ru.walkAndTalk.ui.screens.auth.register.RegisterViewModel
import ru.walkAndTalk.ui.screens.main.chats.ChatsViewModel
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.screens.main.MainViewModel
import ru.walkAndTalk.ui.screens.main.chats.detailchat.ChatViewModel
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsViewModel
import ru.walkAndTalk.ui.screens.onboarding.OnboardingViewModel
import ru.walkAndTalk.ui.screens.main.profile.ProfileViewModel
import ru.walkAndTalk.ui.screens.main.profile.edit.EditProfileViewModel
import ru.walkAndTalk.ui.screens.main.search.SearchViewModel
import ru.walkAndTalk.ui.screens.splash.SplashViewModel
import ru.walkAndTalk.ui.screens.welcome.WelcomeViewModel

private val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::WelcomeViewModel)

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)

    viewModelOf(::MainViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::FeedViewModel)
    viewModelOf(::ChatsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::EventDetailsViewModel)
    viewModelOf(::ChatViewModel)

}

internal val presentationModule = module {
    includes(
        viewModelModule
    )
}