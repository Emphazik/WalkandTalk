package ru.walkAndTalk.ui.screens.profile

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
    private var userId: String? = null

    fun onCreate(id: String) = intent {
        userId = id
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
                cityStatuses = listOf("Новичёк", "Знаток", "Эксперт")
            )
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
            userId?.let { safeUserId ->
                remoteUsersRepository.updateCityKnowledgeLevel(safeUserId, level.id) // Обновление в БД
                reduce { state.copy(selectedCityStatus = status, showCityStatusMenu = false) }
            } ?: run {
                // Логирование или уведомление об ошибке
                println("Ошибка: userId не определен")
                Log.d("CityStatus", "huinya1")
            }
        } else {
            println("Ошибка: статус $status не найден в списке уровней")
            Log.d("CityStatus", "huinya2")
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
            userId?.let { safeUserId ->
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
            userId?.let { id ->
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

    fun onSaveBio() = intent {
        userId?.let { safeUserId ->
            remoteUsersRepository.updateBio(safeUserId, state.newBio) // Обновление в БД
            reduce { state.copy(bio = state.newBio, isEditingBio = false) }
        } ?: run {
            println("Ошибка: userId не определен при сохранении био")
        }
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

    fun onSaveGoals() = intent {
        userId?.let { safeUserId ->
            remoteUsersRepository.updateGoals(safeUserId, state.newGoals) // Обновление в БД
            reduce { state.copy(goals = state.newGoals, isEditingGoals = false) }
        } ?: run {
            println("Ошибка: userId не определен при сохранении целей")
        }
    }

    fun onCancelGoals() = intent {
        reduce { state.copy(isEditingGoals = false, newGoals = state.goals ?: "") }
    }
}





