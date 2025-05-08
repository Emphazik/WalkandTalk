package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.data.repository.LocalDataStoreRepositoryImpl
import ru.walkAndTalk.data.repository.RemoteUsersRepositoryImpl
import ru.walkAndTalk.data.repository.StorageRepositoryImpl
import ru.walkAndTalk.data.repository.VKUsersRepositoryImpl
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository
import ru.walkAndTalk.domain.repository.VKUsersRepository

private val networkModule = module {
    singleOf(::SupabaseWrapper)
}

private val repositoryModule = module {
    singleOf(::LocalDataStoreRepositoryImpl) { bind<LocalDataStoreRepository>() }
    singleOf(::StorageRepositoryImpl) { bind<StorageRepository>() }
    singleOf(::RemoteUsersRepositoryImpl) { bind<RemoteUsersRepository>() }
    singleOf(::VKUsersRepositoryImpl) { bind<VKUsersRepository>() }
}

internal val dataModule = module {
    includes(
        networkModule,
        repositoryModule
    )
}
