package ru.walkAndTalk.ui.screens.main.feed.announcements

sealed class AnnouncementDetailsSideEffect {
    data object OnNavigateBack : AnnouncementDetailsSideEffect()
    data class ShowError(val message: String) : AnnouncementDetailsSideEffect()
    data class NavigateToEditAnnouncement(val announcementId: String) : AnnouncementDetailsSideEffect()
    data object AnnouncementDeleted : AnnouncementDetailsSideEffect()
}