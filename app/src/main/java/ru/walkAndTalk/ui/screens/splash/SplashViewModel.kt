package ru.walkAndTalk.ui.screens.splash

import org.orbitmvi.orbit.syntax.Syntax
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

class SplashViewModel(
    private val supabaseWrapper: SupabaseWrapper,
    private val localDataStoreRepository: LocalDataStoreRepository,
) : ContainerViewModel<SplashViewState, SplashSideEffect>(
    initialState = SplashViewState()
) {

    override suspend fun Syntax<SplashViewState, SplashSideEffect>.onCreate() {
        reduce { state.copy(isAnimationVisible = true) }
        repeatOnSubscription {
            localDataStoreRepository.isFirstLaunch.collect {
                reduce { state.copy(isFirstLaunch = it) }
            }
        }
    }

    fun onIsFirstLaunchChange() = intent {
         postSideEffect(
             when (state.isFirstLaunch) {
                 true -> SplashSideEffect.OnNavigateOnboarding
                 else -> when (supabaseWrapper.auth.currentSessionOrNull()) {
                     null -> SplashSideEffect.OnNavigateWelcome
                     else -> SplashSideEffect.OnNavigateMain
                 }
             }
         )
    }

}