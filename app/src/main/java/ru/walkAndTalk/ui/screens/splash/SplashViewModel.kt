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
            localDataStoreRepository.saveUserMode("admin") // Сбрасываем при отсутствии пользователя
            Log.d("SplashViewModel", "No user, userMode reset to admin")
            postSideEffect(SplashSideEffect.OnNavigateWelcome)
            return@intent
        }

        try {
            val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
                filter { eq("id", user.id) }
            }.decodeSingle<UserDto>()

            if (userData.isAdmin) {
                val currentUserMode = localDataStoreRepository.userMode.first()
                if (currentUserMode != "user") {
                    localDataStoreRepository.saveUserMode("admin")
                    Log.d("SplashViewModel", "Admin user, userMode reset to admin for userId: ${user.id}")
                }
                val userMode = localDataStoreRepository.userMode.first()
                Log.d("SplashViewModel", "User is admin, userMode: $userMode, userId: ${user.id}")
                if (userMode == "user") {
                    postSideEffect(SplashSideEffect.OnNavigateMain(user.id))
                } else {
                    postSideEffect(SplashSideEffect.OnNavigateAdmin(user.id))
                }
            } else {
                localDataStoreRepository.saveUserMode("user") // Для не-админов всегда пользовательский режим
                Log.d("SplashViewModel", "Non-admin user, userMode set to user, userId: ${user.id}")
                postSideEffect(SplashSideEffect.OnNavigateMain(user.id))
            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Error checking user data", e)
            localDataStoreRepository.saveUserMode("admin")
            postSideEffect(SplashSideEffect.OnNavigateWelcome)
        }
    }
}