package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.data.repository.ChatsRepositoryImpl
import ru.walkAndTalk.data.repository.CityKnowledgeLevelRepositoryImpl
import ru.walkAndTalk.data.repository.EventsRepositoryImpl
import ru.walkAndTalk.data.repository.InterestsRepositoryImpl
import ru.walkAndTalk.data.repository.LocalDataStoreRepositoryImpl
import ru.walkAndTalk.data.repository.RemoteUsersRepositoryImpl
import ru.walkAndTalk.data.repository.StorageRepositoryImpl
import ru.walkAndTalk.data.repository.UserEventRepositoryImpl
import ru.walkAndTalk.data.repository.UserInterestsRepositoryImpl
import ru.walkAndTalk.data.repository.VKUsersRepositoryImpl
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository
import ru.walkAndTalk.domain.repository.UserEventRepository
import ru.walkAndTalk.domain.repository.UserInterestsRepository
import ru.walkAndTalk.domain.repository.VKUsersRepository

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
}

internal val dataModule = module {
    includes(
        networkModule,
        repositoryModule
    )
}
