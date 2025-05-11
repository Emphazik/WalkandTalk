package ru.walkAndTalk.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.UserInterestsRepository

class ProfileViewModel(
    private val remoteUsersRepository: RemoteUsersRepository,
    private val cityKnowledgeLevelRepository: CityKnowledgeLevelRepository,
    private val userInterestsRepository: UserInterestsRepository,
    private val interestsRepository: InterestsRepository
) : ViewModel(), ContainerHost<ProfileViewState, ProfileSideEffect> {

    override val container: Container<ProfileViewState, ProfileSideEffect> =
        container(ProfileViewState())

    fun onCreate(id: String) = intent {
        state.userId = id
        val user = remoteUsersRepository.fetchById(id)
            ?: throw RuntimeException("Ошибка, пользователь не найден")
        val cityStatus = user.cityKnowledgeLevelId?.let { levelId ->
            cityKnowledgeLevelRepository.fetchById(levelId)?.name
        } ?: "Не указано"
        val interests = userInterestsRepository.fetchInterestsForUser(id).map { it.name }
        val availableInterests =
            interestsRepository.fetchAll().map { it.name } // Загружаем все доступные интересы

//        Log.d("ProfileViewModel", user.profileImageUrl)
        reduce {
            state.copy(
                name = user.name,
                selectedCityStatus = cityStatus,
                bio = user.bio,
                goals = user.goals,
                interests = interests,
                photoURL = user.profileImageUrl,
                availableInterests = availableInterests,
                cityStatuses = listOf("Новичёк", "Знаток", "Эксперт"),
                userId = id
            )
        }
    }

    fun refreshData() = intent {
        state.userId?.let { id ->
            val user = remoteUsersRepository.fetchById(id) ?: throw RuntimeException("Ошибка, пользователь не найден")
            val cityStatus = user.cityKnowledgeLevelId?.let { levelId ->
                cityKnowledgeLevelRepository.fetchById(levelId)?.name
            } ?: "Не указано"
            val interests = userInterestsRepository.fetchInterestsForUser(id).map { it.name }
            val availableInterests = interestsRepository.fetchAll().map { it.name }

            reduce {
                state.copy(
                    name = user.name,
                    selectedCityStatus = cityStatus,
                    bio = user.bio,
                    goals = user.goals,
                    interests = interests,
                    photoURL = user.profileImageUrl,
                    availableInterests = availableInterests,
                    cityStatuses = state.cityStatuses
                )
            }
        }
    }

    fun onLogout() = intent {
        try {
            Log.d("ProfileViewModel", "Выход из профиля для userId: ${state.userId}")
            remoteUsersRepository.logout()
            postSideEffect(ProfileSideEffect.OnNavigateExit)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Ошибка при выходе: ${e.message}")
            postSideEffect(ProfileSideEffect.OnNavigateExit)
        }
    }

    fun onEditClick() = intent {
        reduce { state.copy(showEditMenu = true) }
    }

    fun onDismissEditMenu() = intent {
        reduce { state.copy(showEditMenu = false) }
    }

    fun onChangeImageClick() = intent {
        reduce { state.copy(showEditMenu = false) }
        postSideEffect(ProfileSideEffect.LaunchImagePicker)
    }

    fun onCityStatusClick() = intent {
        reduce { state.copy(showCityStatusMenu = true) }
    }

    fun onDismissCityStatusMenu() = intent {
        reduce { state.copy(showCityStatusMenu = false) }
    }

    fun onCityStatusSelected(status: String) = intent {
        val levels = cityKnowledgeLevelRepository.fetchAll()
        val level = levels.find { it.name == status }
        if (level != null) {
            state.userId?.let { safeUserId ->
                Log.d("ProfileViewModel", "Обновление статуса для userId: $safeUserId, levelId: ${level.id}")
                remoteUsersRepository.updateCityKnowledgeLevel(safeUserId, level.id) // Обновление в БД
                refreshData()
                reduce { state.copy(selectedCityStatus = status, showCityStatusMenu = false) }
            } ?: run {
                Log.d("ProfileViewModel", "Ошибка: userId не определен")
            }
        } else {
            Log.d("ProfileViewModel", "Ошибка: статус $status не найден в списке уровней")
        }
    }

    fun onSaveBio() = intent {
        state.userId?.let { safeUserId ->
            Log.d("ProfileViewModel", "Обновление био для userId: $safeUserId, bio: ${state.newBio}")
            remoteUsersRepository.updateBio(safeUserId, state.newBio) // Обновление в БД
            refreshData()
            reduce { state.copy(bio = state.newBio, isEditingBio = false) }
        } ?: run {
            Log.d("ProfileViewModel", "Ошибка: userId не определен при сохранении био")
        }
    }

    fun onSaveGoals() = intent {
        state.userId?.let { safeUserId ->
            Log.d("ProfileViewModel", "Обновление целей для userId: $safeUserId, goals: ${state.newGoals}")
            remoteUsersRepository.updateGoals(safeUserId, state.newGoals) // Обновление в БД
            refreshData()
            reduce { state.copy(goals = state.newGoals, isEditingGoals = false) }
        } ?: run {
            Log.d("ProfileViewModel", "Ошибка: userId не определен при сохранении целей")
        }
    }

    fun onAddInterestClick() = intent {
        reduce { state.copy(showInterestSelection = true) }
    }

    fun onDismissInterestSelection() = intent {
        reduce { state.copy(showInterestSelection = false) }
    }


    fun onInterestSelected(interestName: String) = intent {
        val interest = interestsRepository.fetchAll().find { it.name == interestName }
        interest?.id?.let { interestId ->
            state.userId?.let { safeUserId ->
                // Логика сохранения в user_interests (нужна реализация в UserInterestsRepository)
                userInterestsRepository.addInterest(
                    userId = safeUserId,
                    interestId = interestId
                )
                val updatedInterests = state.interests + interestName
                reduce { state.copy(interests = updatedInterests, showInterestSelection = false) }
            }
        }
    }

    fun onInterestRemoved(interestName: String) = intent {
        val interest = interestsRepository.fetchAll().find { it.name == interestName }
        interest?.id?.let { interestId ->
            state.userId?.let { id ->
                if (id.isNotEmpty()) {
                    userInterestsRepository.removeInterest(id, interestId)
                    val updatedInterests = state.interests - interestName
                    reduce { state.copy(interests = updatedInterests) }
                }
            }
        }
    }

    fun onEditBio() = intent {
        reduce { state.copy(isEditingBio = true, newBio = state.bio ?: "") }
    }

    fun onBioChanged(bio: String) = intent {
        reduce { state.copy(newBio = bio) }
    }

    fun onCancelBio() = intent {
        reduce { state.copy(isEditingBio = false, newBio = state.bio ?: "") }
    }

    fun onEditGoals() = intent {
        reduce { state.copy(isEditingGoals = true, newGoals = state.goals ?: "") }
    }

    fun onGoalsChanged(goals: String) = intent {
        reduce { state.copy(newGoals = goals) }
    }

    fun onCancelGoals() = intent {
        reduce { state.copy(isEditingGoals = false, newGoals = state.goals ?: "") }
    }

    fun onImageSelected(imageUri: Uri) = intent {
        state.userId?.let { userId ->
            try {
                Log.d("ProfileViewModel", "Загрузка изображения для userId: $userId")
                val fileName = "${userId}/profile-${System.currentTimeMillis()}.jpg"
                Log.d("ProfileViewModel", "Имя файла: $fileName")
                remoteUsersRepository.uploadProfileImage(userId, imageUri, fileName) // Загружаем в Storage
                val imageUrl = remoteUsersRepository.getProfileImageUrl(userId, fileName) // Получаем URL
                Log.d("ProfileViewModel", "Загруженное изображение, URL: $imageUrl")
                remoteUsersRepository.updateProfileImageUrl(userId, imageUrl) // Обновляем URL в профиле
                refreshData() // Обновляем данные
                reduce { state.copy(photoURL = imageUrl) } // Обновляем UI
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка загрузки изображения: ${e.message}")
            }
        } ?: run {
            Log.d("ProfileViewModel", "Ошибка: userId не определен при загрузке изображения")
        }
    }
}





