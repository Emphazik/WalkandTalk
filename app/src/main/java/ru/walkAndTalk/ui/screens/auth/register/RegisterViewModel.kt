package ru.walkAndTalk.ui.screens.auth.register

import android.net.Uri
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.annotation.OrbitExperimental
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Bucket
import ru.walkAndTalk.domain.Regex
import ru.walkAndTalk.domain.repository.StorageRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

@Serializable
data class ProfileData(
    @SerialName("user_id")
    val userId: String,
    val interests: List<String> = emptyList(),
    val goals: String = "",
    @SerialName("city_knowledge_level")
    val cityKnowledgeLevel: String = "новичок",
    val bio: String = ""
)

@OptIn(OrbitExperimental::class)
class RegisterViewModel(
    private val supabaseWrapper: SupabaseWrapper,
    private val storageRepository: StorageRepository
) : ContainerViewModel<RegisterViewState, RegisterSideEffect>(
    initialState = RegisterViewState()
) {

    fun onNameChange(newName: String) = blockingIntent {
        reduce { state.copy(name = newName.trim()) }
    }

    fun onPhoneChange(newPhone: String) = blockingIntent {
        reduce { state.copy(phone = newPhone.trim()) }
    }

    fun onEmailChange(newEmail: String) = blockingIntent {
        reduce { state.copy(email = newEmail.trim()) }
    }

    fun onPasswordChange(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword.trim()) }
    }

    fun onProfileImageSelected(uri: Uri) = intent {
        reduce { state.copy(profileImageUri = uri) }
    }

    fun onRegisterClick() = intent {
        reduce { state.copy(isLoading = true, error = null, isSuccess = false) }
        try {
            // Валидация всех полей
            validateInputs()

            // Выходим из текущей сессии, если она есть
            println("Register: Выход из любой существующей сессии...")
            supabaseWrapper.auth.signOut()

            // Регистрация через email в Supabase Auth
            println("Register: Попытка зарегистрироваться с помощью электронной почты: ${state.email}")
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
                println("Register: Полученная сессия, текущий пользователь: ${supabaseWrapper.auth.currentUserOrNull()?.id}")

                // Загрузка фото в Storage или использование заглушки
                val imageUrl = state.profileImageUri?.let { uri ->
                    println("Register: Загрузка изображения профиля...")
                    val fileName = "${user.id}/profile.jpg"
                    storageRepository.upload(Bucket.PROFILE_IMAGES, fileName, uri)
                    storageRepository.createSignedUrl(Bucket.PROFILE_IMAGES, fileName)
                } ?: "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0"
                println("Register: Загруженное изображение, URL: $imageUrl")

                // Сохранение данных в таблицу users
                println("Register: Saving user data to 'users' table...")
                supabaseWrapper.postgrest["users"].insert(
                    mapOf(
                        "id" to user.id,
                        "email" to state.email,
                        "phone" to state.phone,
                        "name" to state.name,
                        "profile_image_url" to imageUrl
                    )
                )
                println("Register: User data saved to 'users' table")

                // Сохранение профиля
                println("Register: Saving profile data to 'profiles' table...")
                supabaseWrapper.postgrest["profiles"].insert(
                    ProfileData(
                        userId = user.id,
                        interests = emptyList(),
                        goals = "",
                        cityKnowledgeLevel = "новичок",
                        bio = ""
                    )
                )
                println("Register: Profile data saved to 'profiles' table")
                reduce { state.copy(isLoading = false, isSuccess = true) }
                postSideEffect(RegisterSideEffect.OnNavigateMain)
            } else {
                throw Exception("Не удалось зарегистрировать или войти пользователю: пользователь null.")
            }
        } catch (e: RestException) {
            println("Register: RestException during registration: ${e.message}")
            val errorMessage = when {
                e.message?.contains("user_already_exists") == true -> "Этот email уже зарегистрирован. Используйте другой email или войдите."
                e.message?.contains("invalid_grant") == true -> "Неверный формат email или пароля."
                else -> e.message ?: "Произошла ошибка при регистрации."
            }
            reduce { state.copy(isLoading = false, error = errorMessage, isSuccess = false) }
        } catch (e: Exception) {
            println("Register: Error during registration: ${e.message}")
            reduce { state.copy(isLoading = false, error = e.message, isSuccess = false) }
        }
    }

    private suspend fun validateInputs() = subIntent {
        // Проверка имени
        if (state.name.isBlank()) {
            throw Exception("Имя не должно быть пустым")
        }

        // Проверка телефона
        if (state.phone.isBlank()) {
            throw Exception("Номер телефона не должен быть пустым")
        }
        if (!state.phone.matches(Regex.PHONE)) {
            throw Exception("Номер телефона должен быть в формате +7 и содержать 10 цифр")
        }

        // Проверка email
        if (state.email.isBlank()) {
            throw Exception("Электронная почта не должна быть пустой")
        }
        if (!state.email.matches(Regex.EMAIL)) {
            throw Exception("Неверный формат электронной почты")
        }

        // Проверка пароля
        if (state.password.length < 6) {
            throw Exception("Пароль должен состоять не менее чем из 6 символов")
        }
    }

    fun onLoginClick() = intent {
        postSideEffect(RegisterSideEffect.OnNavigateLogin)
    }

}
