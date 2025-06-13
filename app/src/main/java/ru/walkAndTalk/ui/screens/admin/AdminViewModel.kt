package ru.walkAndTalk.ui.screens.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.repository.AdminContentRepository
import ru.walkAndTalk.domain.repository.AdminUsersRepository
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository

class AdminViewModel(
    private val usersRepository: AdminUsersRepository,
    private val contentRepository: AdminContentRepository,
    private val supabaseWrapper: SupabaseWrapper,
    private val localDataStoreRepository: LocalDataStoreRepository
) : ViewModel(), ContainerHost<AdminViewState, AdminSideEffect> {
    override val container = container<AdminViewState, AdminSideEffect>(AdminViewState())

    init {
        loadData()
    }

    fun loadData() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val users = usersRepository.fetchAllUsers()
            val events = contentRepository.fetchAllEvents()
            val announcements = contentRepository.fetchAllAnnouncements()
            reduce {
                state.copy(
                    users = users,
                    events = events,
                    announcements = announcements,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Load data error", e)
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(AdminSideEffect.ShowError(e.message ?: "Ошибка загрузки данных"))
        }
    }
    fun navigateToProfile(userId: String) = intent {
        postSideEffect(AdminSideEffect.NavigateToProfile(userId, viewOnly = true))
    }

    fun navigateToAddUser() = intent {
        postSideEffect(AdminSideEffect.NavigateToAddUser)
    }

    fun navigateToEditUser(userId: String) = intent {
        postSideEffect(AdminSideEffect.NavigateToEditUser(userId))
    }

    fun addUser(name: String, email: String, password: String) = intent {
        viewModelScope.launch {
            try {
                contentRepository.addUser(name, email, password)
                loadData() // Обновляем данные
                postSideEffect(AdminSideEffect.UserSaved)
            } catch (e: Exception) {
                postSideEffect(AdminSideEffect.ShowError("Ошибка при создании пользователя: ${e.message}"))
            }
        }
    }

    fun updateUser(userId: String, name: String, email: String, phone: String, isAdmin: Boolean, gender: String?) = intent {
        viewModelScope.launch {
            try {
                val user = usersRepository.fetchUserById(userId) ?: throw Exception("Пользователь не найден")
                usersRepository.updateUser(
                    user.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        isAdmin = isAdmin,
                        gender = gender
                    )
                )
                loadData() // Обновляем данные
                postSideEffect(AdminSideEffect.UserSaved)
            } catch (e: Exception) {
                postSideEffect(AdminSideEffect.ShowError("Ошибка при обновлении пользователя: ${e.message}"))
            }
        }
    }
//    fun addUser(name: String, email: String, password: String) = intent{
//        viewModelScope.launch {
//            try {
//                // TODO: Implement to repository call
//                // Example:
//                // repository.addUser(name, email, password)
//                postSideEffect(AdminSideEffect.UserSaved)
//            } catch (e: Exception) {
//                postSideEffect(AdminSideEffect.ShowError("Ошибка при создании пользователя: ${e.message}"))
//            }
//        }
//    }
//
//    fun updateUser(userId: String, name: String, email: String) = intent{
//        viewModelScope.launch {
//            try {
//                // TODO: Implement to repository call
//                // Example:
//                // repository.updateUser(User(id = userId, name = name, email = email))
//                postSideEffect(AdminSideEffect.UserSaved)
//            } catch (e: Exception) {
//                postSideEffect(AdminSideEffect.ShowError("Ошибка при обновлении пользователя: ${e.message}"))
//            }
//        }
//    }

    fun onUserClick(userId: String) = intent {
        postSideEffect(AdminSideEffect.NavigateToUserProfile(userId))
    }

    fun onEventClick(eventId: String) = intent {
        postSideEffect(AdminSideEffect.NavigateToEventDetails(eventId))
    }

    fun onAnnouncementClick(announcementId: String) = intent {
        postSideEffect(AdminSideEffect.NavigateToAnnouncementDetails(announcementId))
    }

    fun deleteUser(userId: String) = intent {
        try {
            usersRepository.deleteUser(userId)
            val updatedUsers = state.users.filter { it.id != userId }
            reduce { state.copy(users = updatedUsers) }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Delete user error", e)
            postSideEffect(AdminSideEffect.ShowError("Ошибка удаления пользователя: ${e.message}"))
        }
    }

    fun updateEventStatus(eventId: String, status: String) = intent {
        try {
            val event = contentRepository.fetchEventById(eventId) ?: return@intent
            contentRepository.updateEvent(event.copy(status = status))
            val updatedEvents = state.events.map {
                if (it.id == eventId) it.copy(status = status) else it
            }
            reduce { state.copy(events = updatedEvents) }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Update event status error", e)
            postSideEffect(AdminSideEffect.ShowError("Ошибка обновления статуса мероприятия: ${e.message}"))
        }
    }

    fun updateAnnouncementStatus(announcementId: String, status: String) = intent {
        try {
            val announcement =
                contentRepository.fetchAnnouncementById(announcementId) ?: return@intent
            contentRepository.updateAnnouncement(announcement.copy(status = status))
            val updatedAnnouncements = state.announcements.map {
                if (it.id == announcementId) it.copy(status = status) else it
            }
            reduce { state.copy(announcements = updatedAnnouncements) }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Update announcement status error", e)
            postSideEffect(AdminSideEffect.ShowError("Ошибка обновления статуса объявления: ${e.message}"))
        }
    }

    fun onLogout() = intent {
        try {
            localDataStoreRepository.saveUserMode("admin")
            Log.d("AdminViewModel", "User mode reset to admin on logout")
            supabaseWrapper.auth.signOut()
            postSideEffect(AdminSideEffect.NavigateToAuth)
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Logout error", e)
            postSideEffect(AdminSideEffect.ShowError("Ошибка при выходе: ${e.message}"))
        }
    }

    fun switchToUserMode(userId: String) = intent {
        try {
            localDataStoreRepository.saveUserMode("user")
            Log.d("AdminViewModel", "Switched to user mode for userId: $userId")
            postSideEffect(AdminSideEffect.NavigateToMain(userId))
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Switch to user mode error", e)
            postSideEffect(AdminSideEffect.ShowError("Ошибка переключения режима: ${e.message}"))
        }
    }
}