package ru.walkAndTalk.ui.screens.admin

import android.net.Uri
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.User

data class AdminViewState(
    val users: List<User> = emptyList(),
    val events: List<Event> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val profileImageUri: Uri? = null,
    val availableInterests: List<Pair<String, String>> = emptyList(),
    val cityKnowledgeLevels: List<Pair<String, String>> = emptyList()
)