package ru.walkAndTalk.ui.screens.main.profile

import android.net.Uri
import android.util.Log
import org.orbitmvi.orbit.syntax.Syntax
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.UserInterestsRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

class ProfileViewModel(
    private val remoteUsersRepository: RemoteUsersRepository,
    private val cityKnowledgeLevelRepository: CityKnowledgeLevelRepository,
    private val userInterestsRepository: UserInterestsRepository,
    private val interestsRepository: InterestsRepository,
    private val userId: String,
) : ContainerViewModel<ProfileViewState, ProfileSideEffect>(
    initialState = ProfileViewState()
) {

    override suspend fun Syntax<ProfileViewState, ProfileSideEffect>.onCreate() {
        refreshData(listOf("Новичёк", "Знаток", "Эксперт"))
    }

    fun refreshData(cityStatuses: List<String> = emptyList()) = intent {
        val user = remoteUsersRepository.fetchById(userId)
            ?: throw RuntimeException("Ошибка, пользователь не найден")
        val cityStatus = user.cityKnowledgeLevelId?.let { levelId ->
            cityKnowledgeLevelRepository.fetchById(levelId)?.name
        } ?: "Не указано"
        val interests = userInterestsRepository.fetchInterestsForUser(userId).map { it.name }
        val availableInterests = interestsRepository.fetchAll().map { it.name }

        reduce {
            state.copy(
                name = user.name,
                selectedCityStatus = cityStatus,
                bio = user.bio ?: "",
                goals = user.goals,
                interests = interests,
                photoURL = user.profileImageUrl,
                availableInterests = availableInterests,
                cityStatuses = cityStatuses,
                tempSelectedInterests = interests // Инициализируем временный список
            )
        }
    }

    fun onLogout() = intent {
        try {
            Log.d("ProfileViewModel", "Выход из профиля для userId: $userId")
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
            Log.d("ProfileViewModel", "Обновление статуса для userId: $userId, levelId: ${level.id}")
            remoteUsersRepository.updateCityKnowledgeLevel(userId, level.id)
            refreshData()
            reduce { state.copy(selectedCityStatus = status, showCityStatusMenu = false) }
        } else {
            Log.d("ProfileViewModel", "Ошибка: статус $status не найден в списке уровней")
        }
    }

    fun onSaveBio() = intent {
        Log.d("ProfileViewModel", "Обновление био для userId: $userId, bio: ${state.newBio}")
        remoteUsersRepository.updateBio(userId, state.newBio)
        refreshData()
        reduce { state.copy(bio = state.newBio, isEditingBio = false) }
    }

    fun onSaveGoals() = intent {
        Log.d("ProfileViewModel", "Обновление целей для userId: $userId, goals: ${state.newGoals}")
        remoteUsersRepository.updateGoals(userId, state.newGoals)
        refreshData()
        reduce { state.copy(goals = state.newGoals, isEditingGoals = false) }
    }

    fun onAddInterestClick() = intent {
        reduce { state.copy(showInterestSelection = true) }
    }

    fun onDismissInterestSelection() = intent {
        // При закрытии обновляем интересы в базе данных
        val addedInterests = state.tempSelectedInterests.filter { !state.interests.contains(it) }
        val removedInterests = state.interests.filter { !state.tempSelectedInterests.contains(it) }

        addedInterests.forEach { interestName ->
            val interest = interestsRepository.fetchAll().find { it.name == interestName }
            interest?.id?.let { interestId ->
                userInterestsRepository.addInterest(userId = userId, interestId = interestId)
            }
        }

        removedInterests.forEach { interestName ->
            val interest = interestsRepository.fetchAll().find { it.name == interestName }
            interest?.id?.let { interestId ->
                userInterestsRepository.removeInterest(userId, interestId)
            }
        }

        reduce {
            state.copy(
                interests = state.tempSelectedInterests,
                showInterestSelection = false
            )
        }
    }

    fun onInterestToggled(interestName: String) = intent {
        val newTempSelectedInterests = if (state.tempSelectedInterests.contains(interestName)) {
            state.tempSelectedInterests - interestName
        } else {
            state.tempSelectedInterests + interestName
        }
        reduce { state.copy(tempSelectedInterests = newTempSelectedInterests) }
    }

    fun onInterestRemoved(interestName: String) = intent {
        val interest = interestsRepository.fetchAll().find { it.name == interestName }
        interest?.id?.let { interestId ->
            userInterestsRepository.removeInterest(userId, interestId)
            val updatedInterests = state.interests - interestName
            reduce { state.copy(interests = updatedInterests, tempSelectedInterests = updatedInterests) }
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
        try {
            Log.d("ProfileViewModel", "Загрузка изображения для userId: $userId")
            val fileName = "${userId}/profile-${System.currentTimeMillis()}.jpg"
            Log.d("ProfileViewModel", "Имя файла: $fileName")
            remoteUsersRepository.uploadProfileImage(userId, imageUri, fileName)
            val imageUrl = remoteUsersRepository.getProfileImageUrl(userId, fileName)
            Log.d("ProfileViewModel", "Загруженное изображение, URL: $imageUrl")
            remoteUsersRepository.updateProfileImageUrl(userId, imageUrl)
            refreshData()
            reduce { state.copy(photoURL = imageUrl) }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Ошибка загрузки изображения: ${e.message}")
        }
    }
}





