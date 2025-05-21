package ru.walkAndTalk.data.di

import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.walkAndTalk.data.location.LocationService
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.data.repository.AuthRepositoryImpl
import ru.walkAndTalk.data.repository.ChatsRepositoryImpl
import ru.walkAndTalk.data.repository.CityKnowledgeLevelRepositoryImpl
import ru.walkAndTalk.data.repository.EventInterestsRepositoryImpl
import ru.walkAndTalk.data.repository.EventParticipantsRepositoryImpl
import ru.walkAndTalk.data.repository.EventsRepositoryImpl
import ru.walkAndTalk.data.repository.InterestsRepositoryImpl
import ru.walkAndTalk.data.repository.LocalDataStoreRepositoryImpl
import ru.walkAndTalk.data.repository.RemoteUsersRepositoryImpl
import ru.walkAndTalk.data.repository.StorageRepositoryImpl
import ru.walkAndTalk.data.repository.UserEventRepositoryImpl
import ru.walkAndTalk.data.repository.UserInterestsRepositoryImpl
import ru.walkAndTalk.data.repository.VKUsersRepositoryImpl
import ru.walkAndTalk.domain.repository.AuthRepository
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository
import ru.walkAndTalk.domain.repository.EventInterestsRepository
import ru.walkAndTalk.domain.repository.EventParticipantsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
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
}
private val serviceModule = module {
    single { LocationService(get()) } // Предоставляем Context через androidContext()
}

internal val dataModule = module {
    includes(
        networkModule,
        repositoryModule,
        serviceModule
    )
}
