package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.walkAndTalk.data.network.SupabaseWrapper

internal val dataModule = module {
    singleOf(::SupabaseWrapper)
}