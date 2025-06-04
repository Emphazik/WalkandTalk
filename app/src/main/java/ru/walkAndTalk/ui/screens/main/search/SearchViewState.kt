package ru.walkAndTalk.ui.screens.main.search

import ru.walkAndTalk.domain.model.User

data class SearchViewState(
    val users: List<User> = emptyList(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val interestNames: Map<String, String> = emptyMap(),
)