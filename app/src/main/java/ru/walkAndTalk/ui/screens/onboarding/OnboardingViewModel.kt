package ru.walkAndTalk.ui.screens.onboarding

import android.util.Log
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

//class OnboardingViewModel(
//    private val localDataStoreRepository: LocalDataStoreRepository
//) : ContainerViewModel<OnboardingViewState, OnboardingSideEffect>(
//    initialState = OnboardingViewState()
//) {
//
//    fun onStateChange(currentPage: Int, pageCount: Int) = intent {
//        reduce {
//            state.copy(
//                currentPage = currentPage,
//                pageCount = pageCount
//            )
//        }
//    }
//
//    fun onNextClick() = intent {
//        postSideEffect(
//            when (state.currentPage < state.pageCount - 1) {
//                true -> {
//                    val nextPage = state.currentPage + 1
//                    reduce { state.copy(currentPage = nextPage) }
//                    OnboardingSideEffect.OnNextClick(nextPage)
//                }
//                else -> {
//                    localDataStoreRepository.saveIsFirstLaunch(false)
//                    OnboardingSideEffect.OnNavigateWelcome
//                }
//            }
//        )
//    }
//}

class OnboardingViewModel(
    private val localDataStoreRepository: LocalDataStoreRepository
) : ContainerViewModel<OnboardingViewState, OnboardingSideEffect>(
    initialState = OnboardingViewState()
) {

    fun onStateChange(currentPage: Int, pageCount: Int) = intent {
        // Лог начала обработки изменения состояния
        Log.d("OnboardingViewModel", "onStateChange: Начало обработки, currentPage: $currentPage, pageCount: $pageCount")

        reduce {
            state.copy(
                currentPage = currentPage,
                pageCount = pageCount
            )
        }
        // Лог успешного изменения состояния
        Log.d("OnboardingViewModel", "onStateChange: Состояние обновлено, currentPage: $currentPage, pageCount: $pageCount")
    }

    fun onNextClick() = intent {
        // Лог начала обработки клика по "Next"
        Log.d("OnboardingViewModel", "onNextClick: Начало обработки клика, currentPage: ${state.currentPage}, pageCount: ${state.pageCount}")

        postSideEffect(
            when (state.currentPage < state.pageCount - 1) {
                true -> {
                    val nextPage = state.currentPage + 1
                    reduce { state.copy(currentPage = nextPage) }
                    // Лог перехода на следующую страницу
                    Log.d("OnboardingViewModel", "onNextClick: Переход на страницу $nextPage")
                    OnboardingSideEffect.OnNextClick(nextPage)
                }
                else -> {
                    // Лог сохранения данных и завершения онбординга
                    Log.d("OnboardingViewModel", "onNextClick: Завершение онбординга, сохранение isFirstLaunch: false")
                    localDataStoreRepository.saveIsFirstLaunch(false)
                    OnboardingSideEffect.OnNavigateWelcome
                }
            }
        )
        // Лог завершения обработки клика
        Log.d("OnboardingViewModel", "onNextClick: Обработка клика завершена")
    }
}