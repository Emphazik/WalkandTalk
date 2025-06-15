package ru.walkAndTalk.ui.screens.main.feed.events.editevents

sealed class EditEventSideEffect {
    object NavigateBack : EditEventSideEffect()
    data class ShowError(val message: String) : EditEventSideEffect()
    object EventUpdated : EditEventSideEffect()
}