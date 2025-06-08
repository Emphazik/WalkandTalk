package ru.walkAndTalk.ui.screens.main.profile.statistics

data class ReviewInput(
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false
)