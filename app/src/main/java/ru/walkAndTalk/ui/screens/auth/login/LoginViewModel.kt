package ru.walkAndTalk.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.walkAndTalk.data.network.SupabaseWrapper
import java.util.regex.Pattern

@Serializable
data class UserData(val email: String)

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginViewState())
    val state: StateFlow<LoginViewState> = _state

    private val emailPattern: Pattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    fun onEmailChange(newEmail: String) {
        _state.value = _state.value.copy(email = newEmail.trim())
    }

    fun onPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(password = newPassword.trim())
    }

    fun onLoginClick() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, isSuccess = false)
            try {
                // Валидация входных данных
                validateInputs()

                val client = SupabaseWrapper.client
                var loginEmail = _state.value.email

                // Проверка, является ли ввод телефоном
                val isPhone = _state.value.email.matches(Regex("^\\+7\\d{10}\$"))
                if (isPhone) {
                    println("Login: Введен телефон, поиск email в таблице users...")
                    val userData = client.from("users").select {
                        filter { eq("phone", _state.value.email) }
                    }.decodeSingleOrNull<UserData>()
                    loginEmail = userData?.email
                        ?: throw Exception("Телефон не зарегистрирован")
                    println("Login: Найден email: $loginEmail")
                }

                // Выходим из текущей сессии, если она есть
                println("Login: Выход из существующей сессии...")
                client.auth.signOut()

                // Вход через email в Supabase Auth
                println("Login: Попытка входа с email: $loginEmail")
                client.auth.signInWith(Email) {
                    email = loginEmail
                    password = _state.value.password
                }

                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    println("Login: Пользователь успешно вошел, ID: ${user.id}")

                    // Проверка сессии
                    println("Login: Проверка сессии...")
                    if (client.auth.currentSessionOrNull() == null) {
                        throw Exception("Сессия не установлена после входа.")
                    }
                    println("Login: Сессия получена, текущий пользователь: ${client.auth.currentUserOrNull()?.id}")

                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                } else {
                    throw Exception("Не удалось войти: пользователь не найден.")
                }
            } catch (e: RestException) {
                println("Login: RestException при входе: ${e.message}")
                val errorMessage = when {
                    e.message?.contains("invalid_grant") == true -> "Неверный email/телефон или пароль."
                    e.message?.contains("email_not_confirmed") == true -> "Email не подтвержден. Проверьте почту."
                    else -> "Произошла ошибка при входе: ${e.message ?: "неизвестная ошибка"}"
                }
                _state.value = _state.value.copy(isLoading = false, error = errorMessage, isSuccess = false)
            } catch (e: Exception) {
                println("Login: Ошибка при входе: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Произошла ошибка при входе.", isSuccess = false)
            }
        }
    }

    private fun validateInputs() {
        // Проверка email или телефона
        if (_state.value.email.isBlank()) {
            throw Exception("Email или телефон не могут быть пустыми")
        }

        // Проверка формата: email или телефон
        val isEmail = emailPattern.matcher(_state.value.email).matches()
        val isPhone = _state.value.email.matches(Regex("^\\+7\\d{10}\$"))
        if (!isEmail && !isPhone) {
            throw Exception("Введите корректный email или телефон в формате +7 и 10 цифр")
        }

        // Проверка пароля
        if (_state.value.password.length < 6) {
            throw Exception("Пароль должен содержать не менее 6 символов")
        }
    }
}