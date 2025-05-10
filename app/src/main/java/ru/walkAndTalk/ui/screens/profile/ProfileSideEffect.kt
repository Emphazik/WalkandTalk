package ru.walkAndTalk.ui.screens.profile

sealed interface ProfileSideEffect {
//    data object OnNavigateExit : ProfileSideEffect
//    data object OnNavigateEditProfile : ProfileSideEffect
//    data object OnNavigateStatisticUser : ProfileSideEffect
    data object LaunchImagePicker : ProfileSideEffect
}