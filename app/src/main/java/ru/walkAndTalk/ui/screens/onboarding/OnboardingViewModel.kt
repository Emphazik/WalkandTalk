package ru.walkAndTalk.ui.screens.onboarding

import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

class OnboardingViewModel(
    private val localDataStoreRepository: LocalDataStoreRepository
) : ContainerViewModel<OnboardingViewState, OnboardingSideEffect>(
    initialState = OnboardingViewState()
) {

    fun onStateChange(currentPage: Int, pageCount: Int) = intent {
        reduce {
            state.copy(
                currentPage = currentPage,
                pageCount = pageCount
            )
        }
    }

    fun onNextClick() = intent {
        postSideEffect(
            when (state.currentPage < state.pageCount - 1) {
                true -> {
                    val nextPage = state.currentPage + 1
                    reduce { state.copy(currentPage = nextPage) }
                    OnboardingSideEffect.OnNextClick(nextPage)
                }
                else -> {
                    localDataStoreRepository.saveIsFirstLaunch(false)
                    OnboardingSideEffect.OnNavigateWelcome
                }
            }
        )
    }

}