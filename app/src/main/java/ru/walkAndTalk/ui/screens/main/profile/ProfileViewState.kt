package ru.walkAndTalk.ui.screens.main.profile

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileViewState(
    val name: String = "",
    val selectedCityStatus: String = "",
    val bio: String = "",
    val interests: List<String> = emptyList(), // Список имен интересов пользователя
    val goals: String? = "",
    val photoURL: String? = null, // URI-ссылка на изображение в Supabase
    val availableInterests: List<String> = emptyList(), // Список всех доступных интересов
    val showEditMenu: Boolean = false,
    val showCityStatusMenu: Boolean = false,
    val showInterestSelection: Boolean = false, // Состояние для показа меню выбора интересов
    val cityStatuses: List<String> = emptyList(),
    val isEditingBio: Boolean = false,
    val isEditingGoals: Boolean = false,
    val newBio: String = "",
    val newGoals: String = ""
)
