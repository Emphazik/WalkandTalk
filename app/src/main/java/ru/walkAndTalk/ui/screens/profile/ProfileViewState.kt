package ru.walkAndTalk.ui.screens.profile

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.Interest

@Immutable
data class ProfileViewState(
    val name: String? = null,
    val selectedCityStatus: String? = null,
    val bio: String? = null,
    val interests: List<String> = emptyList(), // Список имен интересов пользователя
    val goals: String? = null,
    val showEditMenu: Boolean = false,
    val showCityStatusMenu: Boolean = false,
    val cityStatuses: List<String> = listOf("Новичок", "Знаток", "Эксперт"),
    val photoURL: String? = null, // URI-ссылка на изображение в Supabase
    val availableInterests: List<String> = emptyList(), // Список всех доступных интересов
    val showInterestSelection: Boolean = false // Состояние для показа меню выбора интересов
)
