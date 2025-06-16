package ru.walkAndTalk.ui.screens.main.profile

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.syntax.Syntax
import ru.walkAndTalk.data.location.LocationService
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.CityKnowledgeLevelRepository
import ru.walkAndTalk.domain.repository.EventReviewRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.InterestsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.UserInterestsRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class ProfileViewModel(
    private val remoteUsersRepository: RemoteUsersRepository,
    private val cityKnowledgeLevelRepository: CityKnowledgeLevelRepository,
    private val userInterestsRepository: UserInterestsRepository,
    private val interestsRepository: InterestsRepository,
    private val userId: String,
    private val supabaseWrapper: SupabaseWrapper,
    private val locationService: LocationService,
    private val eventReviewRepository: EventReviewRepository, // Добавлено
    private val eventsRepository: EventsRepository, // Добавлено
) : ContainerViewModel<ProfileViewState, ProfileSideEffect>(
    initialState = ProfileViewState()
) {
    override suspend fun Syntax<ProfileViewState, ProfileSideEffect>.onCreate() {
        refreshData(listOf("Новичёк", "Знаток", "Эксперт"))
    }

    suspend fun getUserProfile(): User? {
        return try {
            remoteUsersRepository.fetchById(userId)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error fetching user profile: ${e.message}")
            null
        }
    }

    fun refreshData(cityStatuses: List<String> = emptyList()) = intent {
        val user = remoteUsersRepository.fetchById(userId)
//        val user = remoteUsersRepository.getUser(userId)
            ?: throw RuntimeException("Ошибка, пользователь не найден")
        val cityStatus = user.cityKnowledgeLevelId?.let { levelId ->
            cityKnowledgeLevelRepository.fetchById(levelId)?.name
        } ?: "Не указано"
        val interests = userInterestsRepository.fetchInterestsForUser(userId).map { it.name }
        val availableInterests = interestsRepository.fetchAll().map { it.name }
        val reviews = eventReviewRepository.fetchUserReviews(userId)
        val eventIds = reviews.map { it.eventId }.toSet()
        val events = if (eventIds.isNotEmpty()) {
            eventsRepository.getEventsByIds(eventIds).associateBy { it.id }
        } else {
            emptyMap()
        }

        reduce {
            state.copy(
                name = user.name,
                birthDate = user.birthdate?.let { convertToDisplayFormat(it) } ?: "",
                gender = user.gender, // Добавлено
                city = user.city ?: "",
                selectedCityStatus = cityStatus,
                bio = user.bio ?: "",
                goals = user.goals,
                interests = interests,
                photoURL = user.profileImageUrl,
                availableInterests = availableInterests,
                cityStatuses = cityStatuses,
                tempSelectedInterests = interests,
                newName = user.name,
                newBirthDate = user.birthdate?.let { convertToDisplayFormat(it) } ?: "",
                newGender = user.gender, // Добавлено
                newBio = user.bio ?: "",
                newGoals = user.goals ?: "",
                newCity = user.city ?: state.newCity,
                showReviews = user.showReviews,
                reviews = reviews.associateBy { it.eventId },
                events = events,
                isAdmin = user.isAdmin
            )
        }
    }


    private fun convertToDisplayFormat(dbDate: String): String {
        return try {
            val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfDb.parse(dbDate)
            val sdfDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdfDisplay.format(date)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Ошибка преобразования даты: ${e.message}")
            dbDate
        }
    }

    private fun convertToDbFormat(displayDate: String): String? {
        return try {
            val sdfDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = sdfDisplay.parse(displayDate)
            val sdfDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdfDb.format(date)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Неверный формат даты: $displayDate")
            null
        }
    }


    fun onLogout() = intent {
        try {
            Log.d("ProfileViewModel", "Выход из профиля для userId: $userId")
            remoteUsersRepository.logout()
            postSideEffect(ProfileSideEffect.OnNavigateExit)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Ошибка при выходе: ${e.message}", e)
        }
    }

    fun onUpdateCity() = intent {
        postSideEffect(ProfileSideEffect.RequestLocationPermission)
    }

    fun onLocationPermissionGranted() = intent {
        try {
            locationService.getCurrentLocation().collectLatest { location ->
                val city =
                    locationService.getCityFromCoordinates(location.latitude, location.longitude)
                if (city != null) {
                    reduce { state.copy(newCity = city) }
                    remoteUsersRepository.updateUserCity(userId, city)
                    refreshData(listOf("Новичёк", "Знаток", "Эксперт"))
                } else {
                    reduce { state.copy(birthDateError = "Не удалось определить город") }
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Ошибка получения местоположения: ${e.message}")
            reduce { state.copy(birthDateError = "Ошибка получения местоположения") }
        }
    }

    fun onNameChanged(name: String) = intent {
        val nameError = when {
            name.isBlank() -> "Имя не может быть пустым"
            name.length > 50 -> "Имя не может быть длиннее 50 символов"
            !name.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s'-]+$")) -> "Имя может содержать только буквы, пробелы, дефисы или апострофы"
            else -> null
        }
        reduce { state.copy(newName = name, nameError = nameError) }
    }

    fun onBirthDateChanged(birthDate: String) = intent {
        val birthDateError = validateBirthDate(birthDate)
        reduce { state.copy(newBirthDate = birthDate, birthDateError = birthDateError) }
    }

    private fun validateBirthDate(birthDate: String): String? {
        if (birthDate.isEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(birthDate) ?: return "Неверный формат даты"
            val currentDate = Date()
            val minAgeDate = Calendar.getInstance().apply {
                add(Calendar.YEAR, -13)
            }.time
            when {
                date.after(currentDate) -> "Дата рождения не может быть в будущем"
                date.after(minAgeDate) -> "Вы должны быть старше 13 лет"
                else -> null
            }
        } catch (e: Exception) {
            "Неверный формат даты"
        }
    }

    fun onGenderChanged(gender: String?) = intent {
        reduce { state.copy(newGender = gender) }
    }

    fun onSavePersonalInfo() = intent {
        val nameError = validateName(state.newName)
        val birthDateError = validateBirthDate(state.newBirthDate)
        if (nameError == null && birthDateError == null && state.newName.isNotBlank()) {
            val dbBirthDate = convertToDbFormat(state.newBirthDate)
            try {
                remoteUsersRepository.updateUserProfile(
                    userId = userId,
                    fullName = state.newName,
                    birthDate = dbBirthDate,
                    gender = state.newGender, // Добавлено
                    photoURL = state.photoURL,
                    city = state.newCity.takeIf { it.isNotEmpty() }
                )
                reduce {
                    state.copy(
                        name = state.newName,
                        birthDate = state.newBirthDate,
                        gender = state.newGender, // Добавлено
                        city = state.newCity,
                        isEditingPersonalInfo = false,
                        nameError = null,
                        birthDateError = null
                    )
                }
                refreshData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка обновления профиля: ${e.message}", e)
                reduce { state.copy(nameError = "Ошибка сохранения. Попробуйте снова.") }
            }
        } else {
            reduce { state.copy(nameError = nameError, birthDateError = birthDateError) }
        }
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Имя не может быть пустым"
            name.length > 50 -> "Имя не может быть длиннее 50 символов"
            !name.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s'-]+$")) -> "Имя может содержать только буквы, пробелы, дефисы или апострофы"
            else -> null
        }
    }

    fun onEditPersonalInfo() = intent {
        reduce {
            state.copy(
                isEditingPersonalInfo = true,
                newName = state.name,
                newBirthDate = state.birthDate ?: "",
                newGender = state.gender, // Добавлено
                newCity = state.city ?: "",
                nameError = null,
                birthDateError = null
            )
        }
    }

    fun onCancelPersonalInfo() = intent {
        reduce {
            state.copy(
                isEditingPersonalInfo = false,
                newName = state.name,
                newBirthDate = state.birthDate ?: "",
                newGender = state.gender // Добавлено
            )
        }
    }

    fun onEditBio() = intent {
        reduce {
            state.copy(
                isEditingBio = true,
                newBio = state.bio ?: "",
                bioError = null
            )
        }
    }

    fun onBioChanged(bio: String) = intent {
        val bioError = validateBio(bio)
        reduce { state.copy(newBio = bio, bioError = bioError) }
    }

    private fun validateBio(bio: String): String? {
        if (bio.isEmpty()) return null // Пустое поле допустимо
        return when {
            bio.length > 500 -> "Описание не может быть длиннее 500 символов"
            !bio.matches(Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s.,!?'\"-]+$")) -> "Описание может содержать только буквы, цифры, пробелы и знаки препинания"
            else -> null
        }
    }

    fun onSaveBio() = intent {
        val bioError = validateBio(state.newBio)
        if (bioError == null) {
            try {
                remoteUsersRepository.updateUserProfile(
                    userId = userId,
                    fullName = state.name,
                    birthDate = convertToDbFormat(state.birthDate ?: ""),
                    photoURL = state.photoURL,
                    gender = state.gender,
                    city = state.city,
                    bio = state.newBio.takeIf { it.isNotEmpty() } // Отправляем null, если пусто
                )
                reduce {
                    state.copy(
                        bio = state.newBio,
                        isEditingBio = false,
                        bioError = null
                    )
                }
                refreshData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка обновления описания: ${e.message}", e)
                reduce { state.copy(bioError = "Ошибка сохранения. Попробуйте снова.") }
            }
        } else {
            reduce { state.copy(bioError = bioError) }
        }
    }

    fun onCancelBio() = intent {
        reduce {
            state.copy(
                isEditingBio = false,
                newBio = state.bio ?: "",
                bioError = null
            )
        }
    }

    fun onEditGoals() = intent {
        reduce {
            state.copy(
                isEditingGoals = true,
                newGoals = state.goals ?: "",
                goalsError = null
            )
        }
    }

    fun onGoalsChanged(goals: String) = intent {
        val goalsError = validateGoals(goals)
        reduce { state.copy(newGoals = goals, goalsError = goalsError) }
    }

    private fun validateGoals(goals: String): String? {
        if (goals.isEmpty()) return null // Пустое поле допустимо
        return when {
            goals.length > 300 -> "Цели не могут быть длиннее 300 символов"
            !goals.matches(Regex("^[a-zA-Zа-яА-ЯёЁ0-9\\s.,!?'\"-]+$")) -> "Цели могут содержать только буквы, цифры, пробелы и знаки препинания"
            else -> null
        }
    }

    fun onSaveGoals() = intent {
        val goalsError = validateGoals(state.newGoals)
        if (goalsError == null) {
            try {
                remoteUsersRepository.updateUserProfile(
                    userId = userId,
                    fullName = state.name,
                    birthDate = convertToDbFormat(state.birthDate ?: ""),
                    photoURL = state.photoURL,
                    gender = state.gender,
                    city = state.city,
                    goals = state.newGoals.takeIf { it.isNotEmpty() } // Отправляем null, если пусто
                )
                reduce {
                    state.copy(
                        goals = state.newGoals,
                        isEditingGoals = false,
                        goalsError = null
                    )
                }
                refreshData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка обновления целей: ${e.message}", e)
                reduce { state.copy(goalsError = "Ошибка сохранения. Попробуйте снова.") }
            }
        } else {
            reduce { state.copy(goalsError = goalsError) }
        }
    }

    fun onCancelGoals() = intent {
        reduce {
            state.copy(
                isEditingGoals = false,
                newGoals = state.goals ?: "",
                goalsError = null
            )
        }
    }

    fun onDeleteImage() = intent {
        remoteUsersRepository.updateUserProfile(
            userId = userId,
            fullName = state.name,
            gender = state.gender,
            city = state.city,
            birthDate = state.birthDate?.let { convertToDbFormat(it) },
            photoURL = "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0"
        )
        reduce { state.copy(photoURL = null) }
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
            Log.d(
                "ProfileViewModel",
                "Обновление статуса для userId: $userId, levelId: ${level.id}"
            )
            remoteUsersRepository.updateCityKnowledgeLevel(userId, level.id)
            refreshData()
            reduce { state.copy(selectedCityStatus = status, showCityStatusMenu = false) }
        } else {
            Log.d("ProfileViewModel", "Ошибка: статус $status не найден в списке уровней")
        }
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

    fun formatEventDate(isoDate: String?): String? {
        if (isoDate == null) return null
        return try {
            // Парсим как OffsetDateTime, чтобы сохранить информацию о часовом поясе
            val offsetDateTime = OffsetDateTime.parse(isoDate)
            // Получаем текущее время в часовом поясе устройства
            val zoneId = ZoneId.systemDefault() // Часовой пояс устройства
            val now = LocalDateTime.now(zoneId)
            // Определяем формат в зависимости от даты
            val formatter = when {
                offsetDateTime.toLocalDate() == now.toLocalDate() -> SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                )

                offsetDateTime.year == now.year -> SimpleDateFormat("dd MMM", Locale("ru"))
                else -> SimpleDateFormat("dd MMM yyyy", Locale("ru"))
            }
            // Преобразуем в миллисекунды с учетом смещения
            formatter.format(offsetDateTime.toInstant().toEpochMilli())
        } catch (e: Exception) {
            println("ProfileViewModel: Error parsing date: $e")
            null
        }
    }
}





