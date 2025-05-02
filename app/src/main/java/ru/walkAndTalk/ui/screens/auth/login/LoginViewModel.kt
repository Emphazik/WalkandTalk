package ru.walkAndTalk.ui.screens.auth.login

import com.vk.id.AccessToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.annotation.OrbitExperimental
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Regex
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

@Serializable
data class UserData(val email: String)

@OptIn(OrbitExperimental::class)
class LoginViewModel(
    private val supabaseWrapper: SupabaseWrapper,
    private val localDataStoreRepository: LocalDataStoreRepository,
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
            if (state.email.matches(Regex.EMAIL)) {
                val userData = supabaseWrapper.postgrest.from("users").select {
                    filter { eq("phone", state.email) }
                }.decodeSingleOrNull<UserData>()
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
                postSideEffect(LoginSideEffect.OnNavigateMain)
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
        try {
            localDataStoreRepository.saveAccessToken(accessToken.token)
            postSideEffect(LoginSideEffect.OnNavigateMain)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onVKFail(errorMessage: String) = intent {
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
