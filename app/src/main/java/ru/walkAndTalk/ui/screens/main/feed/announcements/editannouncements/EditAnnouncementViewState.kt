package ru.walkAndTalk.ui.screens.main.feed.announcements.editannouncements

import ru.walkAndTalk.domain.model.Announcement

data class EditAnnouncementViewState(
    val announcement: Announcement? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)