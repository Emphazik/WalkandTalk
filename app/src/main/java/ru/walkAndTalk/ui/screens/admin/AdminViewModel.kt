package ru.walkAndTalk.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Bucket
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.AdminContentRepository
import ru.walkAndTalk.domain.repository.AdminUsersRepository
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository

class AdminViewModel(
    private val usersRepository: AdminUsersRepository,
    private val contentRepository: AdminContentRepository,
    private val supabaseWrapper: SupabaseWrapper,
    private val localDataStoreRepository: LocalDataStoreRepository,
    private val remoteUsersRepository: RemoteUsersRepository,
    private val storageRepository: StorageRepository,
    private val context: Context // Инжектируем Context для SharedPreferences

) : ViewModel(), ContainerHost<AdminViewState, AdminSideEffect> {
    override val container = container<AdminViewState, AdminSideEffect>(AdminViewState())
    private var adminUserId: String? = null

    init {
        // Сохраняем ID администратора при инициализации
        adminUserId = supabaseWrapper.auth.currentUserOrNull()?.id
        Log.d("AdminViewModel", "Saved admin user ID: $adminUserId")
        loadUsers()
        loadData()
    }

    private suspend fun restoreAdminSession() {
        try {
            val prefs = context.getSharedPreferences("admin_credentials", Context.MODE_PRIVATE)
            val adminEmail = prefs.getString("email", null)
            val adminPassword = prefs.getString("password", null)
            if (adminEmail != null && adminPassword != null) {
                supabaseWrapper.auth.signInWith(Email) {
                    this.email = adminEmail
                    this.password = adminPassword
                }
                val restoredUser = supabaseWrapper.auth.currentUserOrNull()
                Log.d("AdminViewModel", "Restored admin session: ${restoredUser?.id}")
            } else {
                Log.e("AdminViewModel", "Admin credentials not found")
            }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Failed to restore admin session: ${e.message}")
        }
    }

    fun saveAdminCredentials(email: String, password: String) {
        val prefs = context.getSharedPreferences("admin_credentials", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
        Log.d("AdminViewModel", "Saved admin credentials: $email")
    }

    fun loadUsers() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val users = usersRepository.fetchAllUsers()
            Log.d("AdminViewModel", "Loaded ${users.size} users")
            reduce { state.copy(isLoading = false, users = users, error = null) }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(AdminSideEffect.ShowError("Ошибка загрузки пользователей: ${e.message}"))
        }
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

    fun onProfileImageSelected(uri: Uri) = intent {
        reduce { state.copy(profileImageUri = uri) }
    }

    fun addUser(
        name: String,
        email: String,
        password: String,
        phone: String = "",
        isAdmin: Boolean = false,
        gender: String? = null,
        profileImageUri: Uri? = null
    ) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            // Выход из текущей сессии, если есть
            supabaseWrapper.auth.signOut()

            // Регистрация нового пользователя
            var user = supabaseWrapper.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            if (user == null) {
                supabaseWrapper.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                user = supabaseWrapper.auth.currentUserOrNull()
            }

            if (user != null) {
                // Загрузка изображения, если выбрано
                val imageUrl = profileImageUri?.let { uri ->
                    val fileName = "${user.id}/profile.jpg"
                    storageRepository.uploadProfileImage(fileName, uri)
                    storageRepository.createSignedUrl(Bucket.PROFILE_IMAGES, fileName)
                } ?: "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0"

                // Сохранение данных пользователя
                remoteUsersRepository.add(
                    User(
                        id = user.id,
                        email = email,
                        phone = phone,
                        name = name,
                        password = password,
                        profileImageUrl = imageUrl.toString(),
                        vkId = null,
                        interestIds = emptyList(),
                        cityKnowledgeLevelId = null,
                        bio = null,
                        goals = null,
                        isAdmin = isAdmin,
                        gender = gender,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )
                )

                reduce { state.copy(isLoading = false, error = null) }
                postSideEffect(AdminSideEffect.UserSaved)
                loadUsers()
            } else {
                throw Exception("Не удалось зарегистрировать пользователя: пользователь null.")
            }
        } catch (e: RestException) {
            val errorMessage = when {
                e.message?.contains("user_already_exists") == true -> "Этот email уже зарегистрирован."
                e.message?.contains("invalid_grant") == true -> "Неверный формат email или пароля."
                else -> "Ошибка при регистрации: ${e.message}"
            }
            reduce { state.copy(isLoading = false, error = errorMessage) }
            postSideEffect(AdminSideEffect.ShowError(errorMessage))
        } catch (e: Exception) {
            val errorMessage = "Ошибка: ${e.message}"
            reduce { state.copy(isLoading = false, error = errorMessage) }
            postSideEffect(AdminSideEffect.ShowError(errorMessage))
        }
    }

//    fun updateUser(userId: String, name: String, email: String, phone: String, isAdmin: Boolean, gender: String?) = intent {
//        viewModelScope.launch {
//            try {
//                val user = usersRepository.fetchUserById(userId) ?: throw Exception("Пользователь не найден")
//                usersRepository.updateUser(
//                    user.copy(
//                        name = name,
//                        email = email,
//                        phone = phone,
//                        isAdmin = isAdmin,
//                        gender = gender
//                    )
//                )
//                loadData() // Обновляем данные
//                postSideEffect(AdminSideEffect.UserSaved)
//            } catch (e: Exception) {
//                postSideEffect(AdminSideEffect.ShowError("Ошибка при обновлении пользователя: ${e.message}"))
//            }
//        }
//    }
fun updateUser(
    userId: String,
    name: String,
    email: String,
    password: String?,
    phone: String,
    isAdmin: Boolean,
    gender: String?,
    profileImageUri: Uri?,
    vkId: String?,
    interestIds: List<String>,
    cityKnowledgeLevelId: String?,
    bio: String?,
    goals: String?
) = intent {
    reduce { state.copy(isLoading = true, error = null) }
    try {
        // Обновление email и password в Supabase Auth, если они изменились
        val currentUser = state.users.find { it.id == userId }
        if (currentUser == null) {
            throw Exception("Пользователь не найден")
        }
        if (email != currentUser.email || password != null) {
            supabaseWrapper.auth.signOut()
            supabaseWrapper.auth.signInWith(Email) {
                this.email = currentUser.email
                this.password = currentUser.password
            }
            if (email != currentUser.email) {
                supabaseWrapper.auth.updateUser {
                    this.email = email
                }
            }
            if (password != null) {
                supabaseWrapper.auth.updateUser {
                    this.password = password
                }
            }
            restoreAdminSession()
        }

        // Загрузка нового изображения профиля, если выбрано
        val imageUrl = profileImageUri?.let { uri ->
            val fileName = "$userId/profile.jpg"
            storageRepository.uploadProfileImage(fileName, uri)
            storageRepository.createSignedUrl(Bucket.PROFILE_IMAGES, fileName)
        } ?: currentUser.profileImageUrl

        // Обновление данных в таблице users
        usersRepository.updateUser(
            User(
                id = userId,
                email = email,
                phone = phone,
                name = name,
                password = password ?: currentUser.password,
                profileImageUrl = imageUrl.toString(),
                vkId = vkId as Long?,
                interestIds = interestIds,
                cityKnowledgeLevelId = cityKnowledgeLevelId,
                bio = bio,
                goals = goals,
                isAdmin = isAdmin,
                gender = gender,
                createdAt = currentUser.createdAt,
                updatedAt = Clock.System.now()
            )
        )

        loadUsers()
        reduce { state.copy(isLoading = false, error = null) }
        postSideEffect(AdminSideEffect.UserSaved)
    } catch (e: RestException) {
        restoreAdminSession()
        val errorMessage = when {
            e.message?.contains("user_already_exists") == true -> "Этот email уже зарегистрирован."
            else -> "Ошибка при обновлении: ${e.message}"
        }
        reduce { state.copy(isLoading = false, error = errorMessage) }
        postSideEffect(AdminSideEffect.ShowError(errorMessage))
    } catch (e: Exception) {
        restoreAdminSession()
        val errorMessage = "Ошибка: ${e.message}"
        reduce { state.copy(isLoading = false, error = errorMessage) }
        postSideEffect(AdminSideEffect.ShowError(errorMessage))
    }
}
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
//    fun deleteUser(userId: String) = intent {
//        reduce { state.copy(isLoading = true, error = null) }
//        try {
//            usersRepository.deleteUser(userId)
//            val updatedUsers = state.users.filter { it.id != userId }
//            reduce { state.copy(users = updatedUsers, isLoading = true, error = null) }
//            postSideEffect(AdminSideEffect.ShowError("Пользователь успешно удален"))
//        } catch (e: Exception) {
//            reduce { state.copy(isLoading = false, error = "Ошибка удаления пользователя: ${e.message}") }
//            postSideEffect(AdminSideEffect.ShowError("Ошибка удаления пользователя: ${e.message}"))
//        }
//    }

    fun updateEventStatus(eventId: String, status: String) = intent {
        try {
            val event = contentRepository.fetchEventById(eventId) ?: return@intent
            contentRepository.updateEvent(event.copy(statusId = status))
            val updatedEvents = state.events.map {
                if (it.id == eventId) it.copy(statusId = status) else it
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
            contentRepository.updateAnnouncement(announcement.copy(statusId = status))
            val updatedAnnouncements = state.announcements.map {
                if (it.id == announcementId) it.copy(statusId = status) else it
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

//    fun onLogout() = intent {
//        supabaseWrapper.auth.signOut()
//        adminUserId = null
//        val prefs = context.getSharedPreferences("admin_credentials", Context.MODE_PRIVATE)
//        prefs.edit().clear().apply()
//        postSideEffect(AdminSideEffect.NavigateToAuth)
//    }
//
//    fun switchToUserMode(userId: String) = intent {
//        Log.d("AdminViewModel", "Switching to user mode with admin ID: $userId")
//        postSideEffect(AdminSideEffect.NavigateToMain(userId))
//    }

}