package ru.walkAndTalk.ui.screens.admin

sealed class AdminSideEffect {
    data class NavigateToUserProfile(val userId: String) : AdminSideEffect()
    data class NavigateToEventDetails(val eventId: String) : AdminSideEffect()
    data class ShowError(val message: String) : AdminSideEffect()
    object NavigateToAuth : AdminSideEffect()
    data class NavigateToMain(val userId: String) : AdminSideEffect()
    object NavigateToAddUser : AdminSideEffect()
    data class NavigateToEditUser(val userId: String) : AdminSideEffect()
    data class NavigateToProfile(val userId: String, val viewOnly: Boolean) : AdminSideEffect()
    object UserSaved : AdminSideEffect()
    data class ShowSuccess(val message: String) : AdminSideEffect()
    data class NavigateToEditEvent(val eventId: String) : AdminSideEffect()
    data class NavigateToAnnouncementDetails(val announcementId: String) : AdminSideEffect()
    data class NavigateToEditAnnouncement(val announcementId: String) : AdminSideEffect()
}

