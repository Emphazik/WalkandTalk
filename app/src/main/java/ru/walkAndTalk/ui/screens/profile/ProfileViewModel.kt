package ru.walkAndTalk.ui.screens.profile

import android.nfc.Tag
import android.util.Log
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.R
import ru.walkAndTalk.domain.model.User
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

        Log.d("ProfileViewModel", user.profileImageUrl)
        reduce {
            state.copy(
                name = user.name,
                selectedCityStatus = cityStatus,
                bio = user.bio,
                goals = user.goals,
                interests = interests,
                photoURL = user.profileImageUrl,
                availableInterests = availableInterests
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
        reduce { state.copy(selectedCityStatus = status, showCityStatusMenu = false) }
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
    }





