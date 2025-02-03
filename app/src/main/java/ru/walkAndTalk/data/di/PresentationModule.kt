package ru.walkAndTalk.data.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.walkAndTalk.ui.screens.auth.login.LoginViewModel

private val viewModelModule = module {
    viewModelOf(::LoginViewModel)
}

internal val presentationModule = module {
    includes(
        viewModelModule
    )
}