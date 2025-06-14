package ru.walkAndTalk.ui.screens.auth.register

import android.net.Uri
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.datetime.Clock.System
import org.orbitmvi.orbit.annotation.OrbitExperimental
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Bucket
import ru.walkAndTalk.domain.Regex
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(OrbitExperimental::class)
class RegisterViewModel(
    private val supabaseWrapper: SupabaseWrapper,
    private val storageRepository: StorageRepository,
    private val remoteUsersRepository: RemoteUsersRepository,
) : ContainerViewModel<RegisterViewState, RegisterSideEffect>(
    initialState = RegisterViewState()
) {

    fun onNameChange(newName: String) = blockingIntent {
        val formattedName = newName.trim().replace("\\s+".toRegex(), " ").let { name ->
            name.split(" ").joinToString(" ") { it.capitalize(Locale.getDefault()) }
        }
        reduce { state.copy(name = formattedName, nameError = null) }
    }

    fun onSurnameChange(newSurname: String) = blockingIntent {
        val formattedSurname = newSurname.trim().replace("\\s+".toRegex(), " ").let { surname ->
            surname.split(" ").joinToString(" ") { it.capitalize(Locale.getDefault()) }
        }
        reduce { state.copy(surname = formattedSurname, surnameError = null) }
    }

    fun onPhoneChange(newPhone: String) = blockingIntent {
        reduce { state.copy(phone = newPhone.trim(), phoneError = null) }
    }

    fun onEmailChange(newEmail: String) = blockingIntent {
        reduce { state.copy(email = newEmail.trim(), emailError = null) }
    }

    fun onPasswordChange(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword.trim(), passwordError = null) }
    }

    fun onProfileImageSelected(uri: Uri) = intent {
        reduce { state.copy(profileImageUri = uri) }
    }

    fun onRegisterClick() = intent {
        reduce { state.copy(isLoading = true, error = null, isSuccess = false) }
        try {
            val isValid = validateInputs()
            if (!isValid) {
                reduce { state.copy(isLoading = false) }
                return@intent
            }

            println("Register: Выход из любой существующей сессии...")
            supabaseWrapper.auth.signOut()

            var user = supabaseWrapper.auth.signUpWith(Email) {
                this.email = state.email
                this.password = state.password
            }

            if (user == null) {
                println("Register: signUpWith вернул null, при попытке войти в систему...")
                supabaseWrapper.auth.signInWith(Email) {
                    this.email = state.email
                    this.password = state.password
                }
                user = supabaseWrapper.auth.currentUserOrNull()
                println("Register: Sign in result: ${user?.id}")
            }

            if (user != null) {
                println("Register: Пользователь успешно зарегистрирован, ID: ${user.id}")

                println("Register: Проверка сессии...")
                if (supabaseWrapper.auth.currentSessionOrNull() == null) {
                    throw Exception("Сессия не устанавливается после регистрации или входа в систему.")
                }

                val imageUrl = state.profileImageUri?.let { uri ->
                    println("Register: Загрузка изображения профиля...")
                    val fileName = "${user.id}/profile.jpg"
                    storageRepository.upload(Bucket.PROFILE_IMAGES, fileName, uri)
                    storageRepository.createSignedUrl(Bucket.PROFILE_IMAGES, fileName)
                }
                    ?: "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0"

                // Сохранение данных в таблицу users, исключаем createdAt и updatedAt
                remoteUsersRepository.add(
                    User(
                        id = user.id,
                        email = state.email,
                        phone = state.phone,
                        name = "${state.name} ${state.surname}",
                        password = state.password,
                        profileImageUrl = imageUrl,
                        vkId = null,
                        interestIds = emptyList(),
                        cityKnowledgeLevelId = null,
                        bio = null,
                        goals = null,
                        createdAt = System.now(),
                        updatedAt = System.now()
                        // Не передаём createdAt и updatedAt, пусть они заполняются на стороне сервера
                    )
                )
                reduce { state.copy(isLoading = false, isSuccess = true) }
                postSideEffect(RegisterSideEffect.OnNavigateMain(user.id))
            } else {
                throw Exception("Не удалось зарегистрировать или войти пользователю: пользователь null.")
            }
        } catch (e: RestException) {
            val errorMessage = when {
                e.message?.contains("user_already_exists") == true -> "Этот email уже зарегистрирован. Используйте другой email или войдите."
                e.message?.contains("invalid_grant") == true -> "Неверный формат email или пароля."
                e.message?.contains("invalid input syntax for type timestamp") == true -> "Ошибка при создании пользователя. Обратитесь в поддержку."
                else -> "Произошла ошибка при регистрации. Обратитесь в поддержку."
            }
            reduce { state.copy(isLoading = false, error = errorMessage, isSuccess = false) }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Сессия не устанавливается") == true -> "Не удалось установить сессию. Попробуйте снова."
                else -> "Произошла ошибка: ${e.message}. Обратитесь в поддержку."
            }
            reduce { state.copy(isLoading = false, error = errorMessage, isSuccess = false) }
        }
    }

    private suspend fun validateInputs(): Boolean = suspendCoroutine { continuation ->
        intent {
            var isValid = true
            var nameError: String? = null
            var surnameError: String? = null
            var phoneError: String? = null
            var emailError: String? = null
            var passwordError: String? = null

            if (state.name.isBlank()) {
                nameError = "Имя не должно быть пустым"
                isValid = false
            } else if (!state.name.matches("[A-Za-zА-Яа-я\\s-]+".toRegex())) {
                nameError = "Имя не должно содержать цифры или недопустимые символы"
                isValid = false
            }

            if (state.surname.isBlank()) {
                surnameError = "Фамилия не должна быть пустой"
                isValid = false
            } else if (!state.surname.matches("[A-Za-zА-Яа-я\\s-]+".toRegex())) {
                surnameError = "Фамилия не должно содержать цифры или недопустимые символы"
                isValid = false
            }

            if (state.phone.isBlank()) {
                phoneError = "Номер телефона не должен быть пустым"
                isValid = false
            } else if (!state.phone.matches(Regex.PHONE)) {
                phoneError = "Номер телефона должен быть в формате +7 и содержать 10 цифр"
                isValid = false
            }

            if (state.email.isBlank()) {
                emailError = "Электронная почта не должна быть пустой"
                isValid = false
            } else if (!state.email.matches(Regex.EMAIL)) {
                emailError = "Неверный формат электронной почты"
                isValid = false
            }

            if (state.password.length < 6) {
                passwordError = "Пароль должен состоять не менее чем из 6 символов"
                isValid = false
            }

            reduce {
                state.copy(
                    nameError = nameError,
                    surnameError = surnameError,
                    phoneError = phoneError,
                    emailError = emailError,
                    passwordError = passwordError
                )
            }

            continuation.resume(isValid)
        }
    }

    fun onLoginClick() = intent {
        postSideEffect(RegisterSideEffect.OnNavigateLogin)
    }

}
