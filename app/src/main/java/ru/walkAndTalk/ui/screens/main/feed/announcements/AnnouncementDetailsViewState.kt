package ru.walkAndTalk.ui.screens.main.feed.announcements

import ru.walkAndTalk.domain.model.Announcement

data class AnnouncementDetailsViewState(
    val announcement: Announcement? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCurrentUserOrganizer: Boolean = false,
    val tagNames: List<String> = emptyList(),
    val activityTypeName: String = ""
)