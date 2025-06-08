package ru.walkAndTalk.ui.screens.main.profile.statistics

sealed class EventStatisticsSideEffect {
    data class ShowError(val message: String) : EventStatisticsSideEffect()
}