package ru.walkAndTalk.ui.screens.splash

import android.util.Log
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.syntax.Syntax
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

class SplashViewModel(
    private val supabaseWrapper: SupabaseWrapper,
    private val localDataStoreRepository: LocalDataStoreRepository
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
        if (state.isFirstLaunch) {
            postSideEffect(SplashSideEffect.OnNavigateOnboarding)
            return@intent
        }

        val user = supabaseWrapper.auth.currentUserOrNull()
        if (user == null) {
            localDataStoreRepository.saveUserMode("user")
            Log.d("SplashViewModel", "No Admin, userMode reset to user")
            postSideEffect(SplashSideEffect.OnNavigateWelcome)
            return@intent
        }

        try {
            val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
                filter { eq("id", user.id) }
            }.decodeSingleOrNull<UserDto>()
            if(userData != null){
                localDataStoreRepository.userMode.collect{ userMode ->
                    if (userMode == "user") {
                        postSideEffect(SplashSideEffect.OnNavigateMain(user.id))
                    } else {
                        postSideEffect(SplashSideEffect.OnNavigateAdmin(user.id))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Error checking user data", e)
            localDataStoreRepository.saveUserMode("admin")
            postSideEffect(SplashSideEffect.OnNavigateWelcome)
        }
    }
}