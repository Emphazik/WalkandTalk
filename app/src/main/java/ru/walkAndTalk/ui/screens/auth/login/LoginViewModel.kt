package ru.walkAndTalk.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.id.AccessToken
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.walkAndTalk.data.network.SupabaseWrapper
import java.util.regex.Pattern
import com.vk.id.VKID
import com.vk.id.VKIDAuthFail
import com.vk.id.auth.VKIDAuthCallback
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository


@Serializable
data class UserData(val email: String)

@OptIn(OrbitExperimental::class)
@Suppress("UNREACHABLE_CODE")
class LoginViewModel(
    private val localDataStoreRepository: LocalDataStoreRepository,
    private val supabaseWrapper: SupabaseWrapper
) : ViewModel(), ContainerHost<LoginViewState, LoginSideEffect> {

    override val container = container<LoginViewState, LoginSideEffect>(LoginViewState())

    private val emailPattern: Pattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    fun onEmailChange(newEmail: String) = intent {
        reduce { state.copy(email = newEmail.trim()) }
    }

    fun onPasswordChange(newPassword: String) = intent {
        reduce { state.copy(password = newPassword.trim()) }
    }

    fun onLoginClick() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            validateInputs()

            val client = supabaseWrapper.client
            var loginEmail = state.email

            val isPhone = state.email.matches(Regex("^\\+7\\d{10}\$"))
            if (isPhone) {
                val userData = client.from("users").select {
                    filter { eq("phone", state.email) }
                }.decodeSingleOrNull<UserData>()
                loginEmail = userData?.email
                    ?: throw Exception("Телефон не зарегистрирован")
            }

            client.auth.signOut()

            client.auth.signInWith(Email) {
                email = loginEmail
                password = state.password
            }

            val user = client.auth.currentUserOrNull()
            if (user != null) {
                if (client.auth.currentSessionOrNull() == null) {
                    throw Exception("Сессия не установлена после входа.")
                }
                reduce { state.copy(isLoading = false, user = null) }
                postSideEffect(LoginSideEffect.NavigateToMainScreen)
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
            postSideEffect(LoginSideEffect.ShowError(errorMessage))
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(LoginSideEffect.ShowError(e.message ?: "Произошла ошибка при входе."))
        }
    }

    fun onVKLoginClick(accessToken: AccessToken) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
           localDataStoreRepository.saveAccessToken(accessToken.token)
            postSideEffect(LoginSideEffect.NavigateToMainScreen)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onVKLoginFailed(errorMessage: String) = intent {
        reduce { state.copy(isLoading = false, error = errorMessage) }
        postSideEffect(LoginSideEffect.ShowError(errorMessage))
    }

    fun onRegisterClick() = intent {
        postSideEffect(LoginSideEffect.OnRegisterClick)
    }

    private suspend fun validateInputs() = subIntent {
        if (state.email.isBlank()) {
            throw Exception("Email или телефон не могут быть пустыми")
        }
        val isEmail = emailPattern.matcher(state.email).matches()
        val isPhone = state.email.matches(Regex("^\\+7\\d{10}\$"))
        if (!isEmail && !isPhone) {
            throw Exception("Введите корректный email или телефон в формате +7 и 10 цифр")
        }
        if (state.password.length < 6) {
            throw Exception("Пароль должен содержать не менее 6 символов")
        }
    }
}
