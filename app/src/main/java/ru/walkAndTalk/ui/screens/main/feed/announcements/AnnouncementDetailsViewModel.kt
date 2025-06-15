package ru.walkAndTalk.ui.screens.main.feed.announcements

import androidx.lifecycle.ViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.ActivityTypesRepository
import ru.walkAndTalk.domain.repository.AnnouncementsRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.time.Instant

class AnnouncementDetailsViewModel(
    private val announcementsRepository: AnnouncementsRepository,
    private val usersRepository: RemoteUsersRepository,
    private val activityTypesRepository: ActivityTypesRepository,
    private val interestsRepository: InterestsRepository,
    private val currentUserId: String
) : ViewModel(), ContainerHost<AnnouncementDetailsViewState, AnnouncementDetailsSideEffect> {

    private val interests = mutableMapOf<String, String>() // tagId -> tagName
    private val activityTypes = mutableMapOf<String, String>() // activityTypeId -> name

    override val container: Container<AnnouncementDetailsViewState, AnnouncementDetailsSideEffect> = container(
        AnnouncementDetailsViewState()
    )

    fun loadAnnouncement(announcementId: String) = intent {
        println("AnnouncementDetailsViewModel: Loading announcement $announcementId")
        reduce { state.copy(isLoading = true, error = null) }
        try {
            interestsRepository.fetchAll().forEach { interest ->
                interests[interest.id] = interest.name
            }
            activityTypesRepository.fetchAll().forEach { activityType ->
                activityTypes[activityType.id] = activityType.name
            }
            val announcement = announcementsRepository.fetchAnnouncementById(announcementId)
            if (announcement != null) {
                val organizerName = usersRepository.fetchById(announcement.creatorId)?.name
                val isCurrentUserOrganizer = announcement.creatorId == currentUserId
                val activityTypeName = activityTypes[announcement.activityTypeId] ?: "Неизвестно"
                println("AnnouncementDetailsViewModel: Loaded announcement $announcementId, activityType=${announcement.activityTypeId}, isOrganizer=$isCurrentUserOrganizer")
                reduce {
                    state.copy(
                        announcement = announcement.copy(organizerName = organizerName),
                        isLoading = false,
                        isCurrentUserOrganizer = isCurrentUserOrganizer,
                        activityTypeName = activityTypeName
                    )
                }
            } else {
                reduce { state.copy(isLoading = false, error = "Объявление не найдено") }
                postSideEffect(AnnouncementDetailsSideEffect.ShowError("Объявление не найдено"))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(AnnouncementDetailsSideEffect.ShowError(e.message ?: "Ошибка загрузки объявления"))
        }
    }

    fun onEditAnnouncementClick(announcementId: String) = intent {
        postSideEffect(AnnouncementDetailsSideEffect.NavigateToEditAnnouncement(announcementId))
    }

    fun onDeleteAnnouncementClick(announcementId: String) = intent {
        try {
            announcementsRepository.deleteAnnouncement(announcementId, currentUserId).onSuccess {
                println("AnnouncementDetailsViewModel: Announcement $announcementId deleted")
                postSideEffect(AnnouncementDetailsSideEffect.AnnouncementDeleted)
            }.onFailure { error ->
                println("AnnouncementDetailsViewModel: Error deleting announcement $announcementId: ${error.message}")
                postSideEffect(AnnouncementDetailsSideEffect.ShowError("Ошибка удаления объявления: ${error.message}"))
            }
        } catch (e: Exception) {
            println("AnnouncementDetailsViewModel: Exception deleting announcement $announcementId: ${e.message}")
            postSideEffect(AnnouncementDetailsSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun onNavigateBack() = intent {
        postSideEffect(AnnouncementDetailsSideEffect.OnNavigateBack)
    }

    fun formatAnnouncementDate(isoDate: String): String {
        val instant = kotlinx.datetime.Instant.parse(isoDate)
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
        return "${dateTime.dayOfMonth} $month ${dateTime.year}, ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
    }
}