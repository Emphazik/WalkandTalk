package ru.walkAndTalk.ui.screens.main.profile

import kotlinx.datetime.Instant
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.EventReview

data class ProfileViewState(
    val name: String = "",
    val newName: String = "",
    val nameError: String? = null,
    val selectedCityStatus: String = "",
    val cityStatuses: List<String> = emptyList(),
    val showCityStatusMenu: Boolean = false,
    val bio: String = "",
    val newBio: String = "",
    val isEditingBio: Boolean = false,
    val interests: List<String> = emptyList(), // Список имен интересов пользователя
    val availableInterests: List<String> = emptyList(), // Список всех доступных интересов
    val showInterestSelection: Boolean = false, // Состояние для показа меню выбора интересов
    val tempSelectedInterests: List<String> = emptyList(), // Временный список для выбора интересов
    val showEditMenu: Boolean = false,
    val goals: String? = "",
    val newGoals: String = "",
    val isEditingGoals: Boolean = false,
    val photoURL: String? = null, // URI-ссылка на изображение в Supabase
    val birthDate: String? = null,
    val newBirthDate: String = "",
    val birthDateError: String? = null,
    val isEditingPersonalInfo: Boolean = false,
    val bioError: String? = null,
    val goalsError: String? = null,
    val city: String? = null,
    val newCity: String = "Не указано",
    val showReviews: Boolean = false, // Новое поле
    val reviews: Map<String, EventReview> = emptyMap(), // Новое поле
    val events: Map<String, Event> = emptyMap(), // Новое
    val isAdmin: Boolean = false,
    val gender: String? = null, // Новое поле: "male", "female", "other"
    val lastLogin: Instant? = null // Новое поле
    )
