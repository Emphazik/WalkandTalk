package ru.walkAndTalk.ui.screens.main

import ru.walkAndTalk.ui.orbit.ContainerViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

class MainViewModel(

) : ContainerViewModel<MainViewState, MainSideEffect>(
    initialState = MainViewState()
) {

    fun onSelectedTabChange(value: Int) = intent {
        reduce { state.copy(selectedTab = value) }
    }

    fun formatEventDate(isoDate: String): String {
        val instant = Instant.parse(isoDate)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = when (dateTime.monthNumber) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            12 -> "декабря"
            else -> ""
        }
        return "${dateTime.dayOfMonth} $month, ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    }
}