package ru.walkAndTalk.ui.screens.main.search

sealed class SearchSideEffect {
    data class NavigateToProfile(val userId: String) : SearchSideEffect()
    data class ShowError(val message: String) : SearchSideEffect()
}