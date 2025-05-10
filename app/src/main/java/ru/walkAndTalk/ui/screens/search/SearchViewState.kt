package ru.walkAndTalk.ui.screens.search

sealed class SearchSideEffect {
    data class NavigateToProfile(val userId: String) : SearchSideEffect()
}