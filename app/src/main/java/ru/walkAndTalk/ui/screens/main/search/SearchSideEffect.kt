package ru.walkAndTalk.ui.screens.main.search

import androidx.compose.runtime.Immutable
import ru.walkAndTalk.domain.model.User

@Immutable
data class SearchViewState(
    val users: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val interestNames: Map<String, String> = emptyMap() // ID интереса -> Название
)