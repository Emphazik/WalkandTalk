package ru.walkAndTalk.ui.screens.auth.login

import android.util.Log
import com.vk.id.AccessToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import org.orbitmvi.orbit.annotation.OrbitExperimental
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Regex
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.VKUsersRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel
import java.util.UUID

@OptIn(OrbitExperimental::class)
class LoginViewModel(
    private val supabaseWrapper: SupabaseWrapper,
    private val vkUsersRepository: VKUsersRepository,
    private val remoteUsersRepository: RemoteUsersRepository,
) : ContainerViewModel<LoginViewState, LoginSideEffect>(
    initialState = LoginViewState()
) {

    fun onEmailChange(newEmail: String) = blockingIntent {
        reduce { state.copy(email = newEmail.trim()) }
    }

    fun onPasswordChange(newPassword: String) = blockingIntent {
        reduce { state.copy(password = newPassword.trim()) }
    }

    fun onLoginClick() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            validateInputs()
            var email = state.email
            if (state.email.matches(Regex.PHONE)) {
                val userData = supabaseWrapper.postgrest.from("users").select {
                    filter { eq("phone", state.email) }
                }.decodeSingleOrNull<UserInfo>()
                email = userData?.email ?: throw Exception("Телефон не зарегистрирован")
            }

            supabaseWrapper.auth.signOut()

            supabaseWrapper.auth.signInWith(Email) {
                this.email = email
                this.password = state.password
            }

            val user = supabaseWrapper.auth.currentUserOrNull()
            if (user != null) {
                if (supabaseWrapper.auth.currentSessionOrNull() == null) {
                    throw Exception("Сессия не установлена после входа.")
                }
                reduce { state.copy(isLoading = false, user = null) }
                postSideEffect(LoginSideEffect.OnNavigateMain(user.id))
            } else {
                throw Exception("Не удалось войти: пользователь не найден.")
            }
        } catch (e: RestException) {
            val errorMessage = when {
                e.message?.contains("invalid_grant") == true -> "Неверный email/телефон или пароль."
                e.message?.contains("email_not_confirmed") == true -> "Email не подтвержден. Проверьте почту."
                else -> "Произошла ошибка при входе: ${e.message ?: "неизвестная ошибка"}"
            }
            reduce { state.copy(isLoading = false, error = errorMessage) }
        } catch (e: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = e.message ?: "Произошла ошибка при входе."
                )
            }
        }
    }

    fun onVKAuth(accessToken: AccessToken) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        reduce { state.copy(isLoading = true, error = null) }
        try {
            Log.d("LoginViewModel", "Starting VK auth with accessToken: ${accessToken.userID}")

            // Сохраняем accessToken для дальнейшего использования
            val savedAccessToken = accessToken
            Log.d("LoginViewModel", "Saved accessToken: ${savedAccessToken.userID}")

            // Получаем данные пользователя из VK API
            val vkUser = vkUsersRepository.fetchUser(accessToken.userID)
            Log.d("LoginViewModel", "Fetched VK user: vkId=${vkUser.vkId}, email=${vkUser.email}, phone=${vkUser.phone}")

            val userId: String
            // Проверяем, есть ли пользователь в базе по vkId
            val existingUser = vkUser.vkId?.let { remoteUsersRepository.fetchByVkId(it) }

            if (existingUser != null) {
                // Пользователь найден, берем его id
                userId = existingUser.id
                Log.d("LoginViewModel", "User found with vkId: ${vkUser.vkId}, userId: $userId")

                // Входим в систему с помощью Supabase Auth (если нужно)
                // Здесь можно добавить вызов supabaseWrapper.auth.signInWith(Email), если у тебя есть пароль
            } else {
                // Пользователь не найден, регистрируем нового через Supabase Auth
                val email = vkUser.email ?: throw IllegalStateException("Email is required for Supabase Auth signup")
                val password = "tempPassword_${UUID.randomUUID()}" // Генерируем временный пароль
                Log.d("LoginViewModel", "Signing up with email: $email")

                val authResponse = supabaseWrapper.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                // Получаем userId из ответа Supabase Auth
                userId = authResponse?.id ?: throw IllegalStateException("Failed to get userId from Supabase Auth")
                Log.d("LoginViewModel", "Supabase Auth signup successful, userId: $userId")

                // Создаем объект User с id из Supabase Auth
                val newUser = User(
                    id = userId,
                    email = vkUser.email,
                    phone = vkUser.phone,
                    name = vkUser.name,
                    profileImageUrl = vkUser.profileImageUrl,
                    vkId = vkUser.vkId,
                    interestIds = emptyList(),
                    cityKnowledgeLevelId = null,
                    bio = null,
                    goals = null,
                    createdAt = System.now(),
                    updatedAt = System.now()
                )

                // Сохраняем пользователя в таблицу users
                remoteUsersRepository.add(newUser.toDto())
                Log.d("LoginViewModel", "New user added to users table with id: $userId")
            }

            postSideEffect(LoginSideEffect.OnNavigateMain(userId))
            reduce { state.copy(isLoading = false) }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "VK auth error: ${e.message}", e)
            reduce { state.copy(isLoading = false, error = "VK auth error: ${e.message}") }
        }
    }

    fun onVKFail(errorMessage: String) = intent {
        Log.d("LoginViewModel", "VK fail: $errorMessage")
        reduce { state.copy(isLoading = false, error = errorMessage) }
    }

    fun onRegisterClick() = intent {
        postSideEffect(LoginSideEffect.OnNavigateRegister)
    }

    private suspend fun validateInputs() = subIntent {
        if (state.email.isBlank()) {
            throw Exception("Email или телефон не могут быть пустыми")
        }
        if (!state.email.matches(Regex.EMAIL) && !state.email.matches(Regex.PHONE)) {
            throw Exception("Введите корректный email или телефон в формате +7 и 10 цифр")
        }
        if (state.password.length < 6) {
            throw Exception("Пароль должен содержать не менее 6 символов")
        }
    }
}
