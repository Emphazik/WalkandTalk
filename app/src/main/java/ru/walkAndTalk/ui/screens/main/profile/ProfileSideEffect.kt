package ru.walkAndTalk.ui.screens.main.profile

sealed interface ProfileSideEffect {
    data object OnNavigateExit : ProfileSideEffect
//    data object OnNavigateStatisticUser : ProfileSideEffect
    data object LaunchImagePicker : ProfileSideEffect
    data object RequestLocationPermission : ProfileSideEffect
}