package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.ui.screens.admin.AdminViewModel
import ru.walkAndTalk.ui.screens.auth.login.LoginViewModel
import ru.walkAndTalk.ui.screens.auth.register.RegisterViewModel
import ru.walkAndTalk.ui.screens.main.MainViewModel
import ru.walkAndTalk.ui.screens.main.chats.ChatsViewModel
import ru.walkAndTalk.ui.screens.main.chats.detailchat.ChatViewModel
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel
import ru.walkAndTalk.ui.screens.main.feed.events.EventDetailsViewModel
import ru.walkAndTalk.ui.screens.main.feed.events.editevents.EditEventViewModel
import ru.walkAndTalk.ui.screens.main.feed.notifications.NotificationsViewModel
import ru.walkAndTalk.ui.screens.main.profile.ProfileViewModel
import ru.walkAndTalk.ui.screens.main.profile.statistics.EventStatisticsViewModel
import ru.walkAndTalk.ui.screens.main.search.SearchViewModel
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
    viewModelOf(::ProfileViewModel)
//    viewModelOf(::FeedViewModel)
    viewModel {
        FeedViewModel(
            fetchFeedItemsUseCase = get(),
            createEventUseCase = get(),
            createAnnouncementUseCase = get(),
            eventParticipantsRepository = get(),
            chatsRepository = get(),
            currentUserId = get<SupabaseWrapper>().auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated"),
            interestsRepository = get(),
            storageRepository = get(),
            remoteUsersRepository = get(),
            activityTypesRepository = get(),
        )
    }
    viewModelOf(::ChatsViewModel)
    viewModelOf(::SearchViewModel)
    //viewModelOf(::EventDetailsViewModel)
    //viewModelOf(::ChatViewModel)
    viewModelOf(::EventStatisticsViewModel)
    factory { (chatId: String, userId: String) ->
        ChatViewModel(
            chatId = chatId,
            userId = userId,
            chatsRepository = get(),
            messagesRepository = get(),
            usersRepository =  get(),
            supabaseWrapper = get(),
        )
    }
    factory {
        EventDetailsViewModel(
            eventsRepository = get(),
            eventParticipantsRepository = get(),
            chatsRepository = get(),
            currentUserId = get<SupabaseWrapper>().auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User not authenticated"),
            usersRepository = get(),
            interestsRepository = get(),
        )
    }
//    viewModelOf(::NotificationsViewModel)
    viewModel { (userId: String) ->
        NotificationsViewModel(
            notificationsRepository = get(),
            currentUserId = userId
        )
    }
    viewModelOf(::AdminViewModel)
    viewModelOf(::EditEventViewModel)

}

internal val presentationModule = module {
    includes(
        viewModelModule
    )
}