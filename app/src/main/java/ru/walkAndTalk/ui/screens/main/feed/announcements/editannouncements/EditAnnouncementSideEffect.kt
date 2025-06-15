package ru.walkAndTalk.ui.screens.main.feed.announcements.editannouncements

sealed class EditAnnouncementSideEffect {
    data object NavigateBack : EditAnnouncementSideEffect()
    data class ShowError(val message: String) : EditAnnouncementSideEffect()
    data object AnnouncementUpdated : EditAnnouncementSideEffect()
    data object AnnouncementDeleted : EditAnnouncementSideEffect()
}