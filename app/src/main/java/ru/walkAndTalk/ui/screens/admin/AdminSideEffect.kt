package ru.walkAndTalk.ui.screens.admin

sealed class AdminSideEffect {
    data class NavigateToUserProfile(val userId: String) : AdminSideEffect()
    data class NavigateToEventDetails(val eventId: String) : AdminSideEffect()
    data class NavigateToAnnouncementDetails(val announcementId: String) : AdminSideEffect()
    data class ShowError(val message: String) : AdminSideEffect()
    object NavigateToAuth : AdminSideEffect()
    data class NavigateToMain(val userId: String) : AdminSideEffect()
}