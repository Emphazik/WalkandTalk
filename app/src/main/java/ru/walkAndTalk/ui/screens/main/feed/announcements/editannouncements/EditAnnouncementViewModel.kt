package ru.walkAndTalk.ui.screens.main.feed.announcements.editannouncements

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.repository.AnnouncementsRepository
import ru.walkAndTalk.ui.screens.main.feed.FeedViewModel

class EditAnnouncementViewModel(
    private val announcementsRepository: AnnouncementsRepository,
    private val feedViewModel: FeedViewModel,
    private val currentUserId: String
) : ViewModel(), ContainerHost<EditAnnouncementViewState, EditAnnouncementSideEffect> {

    override val container: Container<EditAnnouncementViewState, EditAnnouncementSideEffect> = container(
        EditAnnouncementViewState()
    )

    fun loadAnnouncement(announcementId: String) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val announcement = announcementsRepository.fetchAnnouncementById(announcementId)
            if (announcement != null) {
                if (announcement.creatorId != currentUserId) {
                    reduce { state.copy(isLoading = false, error = "Нет прав для редактирования") }
                    postSideEffect(EditAnnouncementSideEffect.ShowError("Нет прав для редактирования"))
                } else {
                    reduce { state.copy(announcement = announcement, isLoading = false) }
                }
            } else {
                reduce { state.copy(isLoading = false, error = "Объявление не найдено") }
                postSideEffect(EditAnnouncementSideEffect.ShowError("Объявление не найдено"))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(EditAnnouncementSideEffect.ShowError(e.message ?: "Ошибка загрузки"))
        }
    }

    fun updateAnnouncement(announcement: Announcement) = intent {
        try {
            val pendingStatusId = feedViewModel.getEventStatusId("pending")
                ?: "ac32ffcc-8f69-4b71-8a39-877c1eb95e04"
            val updatedAnnouncement = announcement.copy(
                statusId = pendingStatusId
            )
            announcementsRepository.updateAnnouncement(updatedAnnouncement).onSuccess {
                postSideEffect(EditAnnouncementSideEffect.AnnouncementUpdated)
            }.onFailure { error ->
                postSideEffect(EditAnnouncementSideEffect.ShowError("Ошибка обновления: ${error.message}"))
            }
        } catch (e: Exception) {
            postSideEffect(EditAnnouncementSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun deleteAnnouncement() = intent {
        try {
            state.announcement?.let { announcement ->
                announcementsRepository.deleteAnnouncement(announcement.id, currentUserId).onSuccess {
                    postSideEffect(EditAnnouncementSideEffect.AnnouncementDeleted)
                }.onFailure { error ->
                    postSideEffect(EditAnnouncementSideEffect.ShowError("Ошибка удаления: ${error.message}"))
                }
            }
        } catch (e: Exception) {
            postSideEffect(EditAnnouncementSideEffect.ShowError("Ошибка: ${e.message}"))
        }
    }

    fun navigateBack() = intent {
        postSideEffect(EditAnnouncementSideEffect.NavigateBack)
    }
}