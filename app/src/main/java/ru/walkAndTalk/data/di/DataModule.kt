package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.data.repository.LocalDataStoreRepositoryImpl
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository

internal val dataModule = module {
    singleOf(::SupabaseWrapper)
    singleOf(::LocalDataStoreRepositoryImpl) {
        bind<LocalDataStoreRepository>()
    }
}
