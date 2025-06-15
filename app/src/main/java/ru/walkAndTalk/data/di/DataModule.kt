package ru.walkAndTalk.data.di

import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.walkAndTalk.data.location.LocationService
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.data.repository.ActivityTypesRepositoryImpl
import ru.walkAndTalk.data.repository.AdminContentRepositoryImpl
import ru.walkAndTalk.data.repository.AdminUsersRepositoryImpl
import ru.walkAndTalk.data.repository.AnnouncementsRepositoryImpl
import ru.walkAndTalk.data.repository.AuthRepositoryImpl
import ru.walkAndTalk.data.repository.ChatsRepositoryImpl
import ru.walkAndTalk.data.repository.CityKnowledgeLevelRepositoryImpl
import ru.walkAndTalk.data.repository.EventInterestsRepositoryImpl
import ru.walkAndTalk.data.repository.EventParticipantsRepositoryImpl
import ru.walkAndTalk.data.repository.EventReviewRepositoryImpl
import ru.walkAndTalk.data.repository.EventsRepositoryImpl
import ru.walkAndTalk.data.repository.FeedRepositoryImpl
import ru.walkAndTalk.data.repository.InterestsRepositoryImpl
import ru.walkAndTalk.data.repository.LocalDataStoreRepositoryImpl
import ru.walkAndTalk.data.repository.MessagesRepositoryImpl
import ru.walkAndTalk.data.repository.NotificationsRepositoryImpl
import ru.walkAndTalk.data.repository.RemoteUsersRepositoryImpl
import ru.walkAndTalk.data.repository.StorageRepositoryImpl
import ru.walkAndTalk.data.repository.UserEventRepositoryImpl
import ru.walkAndTalk.data.repository.UserInterestsRepositoryImpl
import ru.walkAndTalk.data.repository.VKUsersRepositoryImpl
import ru.walkAndTalk.data.usecase.CreateAnnouncementUseCase
import ru.walkAndTalk.data.usecase.CreateEventUseCase
import ru.walkAndTalk.data.usecase.FetchFeedItemsUseCase
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.domain.repository.ActivityTypesRepository
import ru.walkAndTalk.domain.repository.AdminContentRepository
import ru.walkAndTalk.domain.repository.AdminUsersRepository
import ru.walkAndTalk.domain.repository.AnnouncementsRepository
import ru.walkAndTalk.domain.repository.AuthRepository
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository
import ru.walkAndTalk.domain.repository.EventInterestsRepository
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventReviewRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.FeedRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.domain.repository.MessagesRepository
import ru.walkAndTalk.domain.repository.NotificationsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository
import ru.walkAndTalk.domain.repository.UserEventRepository
import ru.walkAndTalk.domain.repository.UserInterestsRepository
import ru.walkAndTalk.domain.repository.VKUsersRepository
import kotlin.math.sin

private val networkModule = module {
    singleOf(::SupabaseWrapper)
}

private val repositoryModule = module {
    singleOf(::LocalDataStoreRepositoryImpl) { bind<LocalDataStoreRepository>() }
    singleOf(::StorageRepositoryImpl) { bind<StorageRepository>() }
    singleOf(::RemoteUsersRepositoryImpl) { bind<RemoteUsersRepository>() }
    singleOf(::VKUsersRepositoryImpl) { bind<VKUsersRepository>() }
    singleOf(::EventsRepositoryImpl) { bind<EventsRepository>() }
    singleOf(::ChatsRepositoryImpl) {bind<ChatsRepository>()}
    singleOf(::CityKnowledgeLevelRepositoryImpl) {bind<CityKnowledgeLevelRepository>() }
    singleOf(::InterestsRepositoryImpl) {bind<InterestsRepository>()}
    singleOf(::UserInterestsRepositoryImpl) {bind<UserInterestsRepository>()}
    singleOf(::UserEventRepositoryImpl) {bind<UserEventRepository>()}
    singleOf(::EventParticipantsRepositoryImpl) {bind<EventParticipantsRepository>()}
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::EventInterestsRepositoryImpl) {bind<EventInterestsRepository>()}
    singleOf(::MessagesRepositoryImpl) {bind<MessagesRepository>()}
    singleOf(::EventReviewRepositoryImpl) {bind<EventReviewRepository>()}
    singleOf(::NotificationsRepositoryImpl) {bind<NotificationsRepository>()}
    singleOf(::AdminContentRepositoryImpl) { bind<AdminContentRepository>() }
    singleOf(::AdminUsersRepositoryImpl) { bind<AdminUsersRepository>() }
    singleOf(::FeedRepositoryImpl) { bind<FeedRepository>() }
    singleOf(::AnnouncementsRepositoryImpl) { bind<AnnouncementsRepository>() }
    singleOf(::ActivityTypesRepositoryImpl) { bind<ActivityTypesRepository>() }
}
private val serviceModule = module {
    single { LocationService(get()) } // Предоставляем Context через androidContext()
}

private val useCaseModule = module {
    singleOf(::FetchFeedItemsUseCase)
    singleOf(::CreateEventUseCase)
    singleOf(::CreateAnnouncementUseCase)
}

internal val dataModule = module {
    includes(
        networkModule,
        repositoryModule,
        serviceModule,
        useCaseModule
    )
}
