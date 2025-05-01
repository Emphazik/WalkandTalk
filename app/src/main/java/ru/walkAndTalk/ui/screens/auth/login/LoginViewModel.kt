package ru.walkAndTalk.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.id.AccessToken
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import org.json.JSONObject
import ru.walkAndTalk.data.network.SupabaseWrapper
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random
import com.vk.api.sdk.requests.VKRequest


@Serializable
data class UserData(val email: String)

@Suppress("UNREACHABLE_CODE")
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
                validateInputs()

                val client = SupabaseWrapper.client
                var loginEmail = _state.value.email

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

                println("Login: Выход из существующей сессии...")
                client.auth.signOut()

                println("Login: Попытка входа с email: $loginEmail")
                client.auth.signInWith(Email) {
                    email = loginEmail
                    password = _state.value.password
                }

                val user = client.auth.currentUserOrNull()
                if (user != null) {
                    println("Login: Пользователь успешно вошел, ID: ${user.id}")

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
                _state.value =
                    _state.value.copy(isLoading = false, error = errorMessage, isSuccess = false)
            } catch (e: Exception) {
                println("Login: Ошибка при входе: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Произошла ошибка при входе.",
                    isSuccess = false
                )
            }
        }
    }

    fun onVKLoginClick(accessToken: AccessToken) {
        viewModelScope.launch {
//            _state.value = _state.value.copy(isLoading = true, error = null, isSuccess = false)
//            try {
//                val client = SupabaseWrapper.client
//
//                val vkUserData = fetchVKUserData(accessToken)
//
//                val vkId = vkUserData.vkId
//                val vkEmail = vkUserData.email
//                val vkName = vkUserData.name
//
//                // 2. Пытаемся найти пользователя в Supabase по vk_id
//                val existingUser = client.from("users").select {
//                    filter { eq("vk_id", vkId) }
//                }.decodeSingleOrNull<Map<String, Any>>()
//
//                if (existingUser != null) {
//                    println("VK Login: Пользователь найден, вход...")
//
//                    val email = existingUser["email"] as String
//                    val passwordHash = existingUser["password_hash"] as? String
//                        ?: throw Exception("Не найден пароль для VK пользователя")
//
//                    // Входим по email + сохранённому паролю
//                    client.auth.signInWith(Email) {
//                        this.email = email
//                        this.password = passwordHash
//                    }
//                } else {
//                    println("VK Login: Новый пользователь, регистрация...")
//
//                    // Генерируем пароль
//                    val randomPassword = generateRandomPassword()
//
//                    // Регистрируем пользователя в Supabase Auth
//                    val signUpResult = client.auth.signUpWith(Email) {
//                        this.email = vkEmail
//                        this.password = randomPassword
//                    }
//
//                    val user = client.auth.currentUserOrNull()
//                        ?: throw Exception("Ошибка регистрации через VK")
//                    // Сохраняем в таблицу users
//                    client.from("users").insert(
//                        mapOf(
//                            "id" to user.id,
//                            "email" to vkEmail,
//                            "phone" to "",
//                            "name" to vkName,
//                            "profile_image_url" to "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=...",
//                            "vk_id" to vkId,
//                            "password_hash" to randomPassword // !!! Вставляем реальный пароль, который создали
//                        )
//                    )
//                }
//
//                val currentUser = client.auth.currentUserOrNull()
//                if (currentUser != null) {
//                    println("VK Login: Успех, пользователь ID: ${currentUser.id}")
//                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
//                } else {
//                    throw Exception("Не удалось установить сессию после VK авторизации.")
//                }
//
//            } catch (e: Exception) {
//                println("VK Login: Ошибка: ${e.message}")
//                _state.value = _state.value.copy(
//                    isLoading = false,
//                    error = "Ошибка входа через VK: ${e.message ?: "Неизвестная ошибка"}",
//                    isSuccess = false
//                )
//            }
        }
    }

//    fun onVKLoginClick(accessToken: AccessToken) {
//        viewModelScope.launch {
//            _state.value = _state.value.copy(isLoading = true, error = null, isSuccess = false)
//            try {
//                val client = SupabaseWrapper.client
//                println("VK Login: Попытка авторизации с VK ID, token: ${accessToken.token}")
//
//                // Получение данных пользователя через VK API
//                val vkUserData = fetchVKUserData(accessToken)
//                val vkId = vkUserData.vkId
//                val vkEmail = vkUserData.email
//                val vkName = vkUserData.name
//
//                // Проверка, существует ли пользователь с этим VK ID
//                val existingUser = client.from("users").select {
//                    filter { eq("vk_id", vkId) }
//                }.decodeSingleOrNull<Map<String, Any>>()
//
//                if (existingUser == null) {
//                    println("VK Login: Пользователь с VK ID $vkId не найден, регистрация...")
//                    // Регистрация нового пользователя в Supabase Auth
//                    client.auth.signUpWith(Email) {
//                        email = vkEmail
//                        password = vkPassword
//                    }
//
//                    val user = client.auth.currentUserOrNull()
//                    if (user != null) {
//                        // Сохранение данных в таблицу users
//                        client.from("users").insert(
//                            mapOf(
//                                "id" to user.id,
//                                "email" to vkEmail,
//                                "phone" to "",
//                                "name" to vkName,
//                                "profile_image_url" to "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0",
//                                "vk_id" to vkId
//                            )
//                        )
//
//                        // Сохранение профиля
//                        client.from("profiles").insert(
//                            mapOf(
//                                "user_id" to user.id,
//                                "interests" to emptyList<String>(),
//                                "goals" to "",
//                                "city_knowledge_level" to "новичок",
//                                "bio" to ""
//                            )
//                        )
//                        println("VK Login: Новый пользователь зарегистрирован, ID: ${user.id}")
//                    } else {
//                        throw Exception("Не удалось зарегистрировать пользователя через VK.")
//                    }
//                } else {
//                    println("VK Login: Пользователь с VK ID $vkId найден, вход...")
//                    // Вход существующего пользователя
//                    client.auth.signInWith(Email) {
//                        email = existingUser["email"] as String
//                        password = existingUser["password"] as String
//                    }
//                }
//
//                val user = client.auth.currentUserOrNull()
//                if (user != null && client.auth.currentSessionOrNull() != null) {
//                    println("VK Login: Сессия установлена, пользователь: ${user.id}")
//                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
//                } else {
//                    throw Exception("Не удалось установить сессию после VK авторизации.")
//                }
//            } catch (e: Exception) {
//                println("VK Login: Ошибка при VK авторизации: ${e.message}")
//                _state.value = _state.value.copy(
//                    isLoading = false,
//                    error = "Ошибка входа через VK: ${e.message ?: "неизвестная ошибка"}",
//                    isSuccess = false
//                )
//            }
//        }
//    }

    fun onVKLoginClickFailed(errorMessage: String) {
        _state.value = _state.value.copy(
            isLoading = false,
            error = errorMessage,
            isSuccess = false
        )
    }

    private fun validateInputs() {
        if (_state.value.email.isBlank()) {
            throw Exception("Email или телефон не могут быть пустыми")
        }
        val isEmail = emailPattern.matcher(_state.value.email).matches()
        val isPhone = _state.value.email.matches(Regex("^\\+7\\d{10}\$"))
        if (!isEmail && !isPhone) {
            throw Exception("Введите корректный email или телефон в формате +7 и 10 цифр")
        }
        if (_state.value.password.length < 6) {
            throw Exception("Пароль должен содержать не менее 6 символов")
        }
    }

    private fun generateRandomPassword(): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..16).map { chars.random(Random) }.joinToString("")
    }

    private suspend fun fetchVKUserData(accessToken: AccessToken): VKUserData {
        return suspendCancellableCoroutine { continuation ->
            // 1. Устанавливаем токен в VK SDK
            val request = VKRequest<JSONObject>("users.get").apply {
                addParam("fields", "first_name,email")
                addParam("access_token", accessToken.token) // Передаём токен в запрос
                addParam("v", "5.199") // Актуальная версия VK API
            }
            VK.execute(request, object : VKApiCallback<JSONObject> {
                override fun success(result: JSONObject) {
                    try {
                        val responseArray = result.getJSONArray("response")
                        val userJson = responseArray.getJSONObject(0)
                        val vkId = userJson.getString("id")
                        val vkName = userJson.getString("first_name")
                        val vkEmail = "vk_$vkId@walkandtalk.com" // генерируем сами
                        val vkPassword = generateRandomPassword() // генерируем сами
                        continuation.resume(VKUserData(vkId, vkEmail, vkPassword, vkName))
                    } catch (e: Exception) {
                        continuation.resumeWithException(Exception("Ошибка обработки данных VK: ${e.message}"))
                    }
                }

                override fun fail(error: Exception) {
                    continuation.resumeWithException(Exception("Ошибка VK API: ${error.message}"))
                }
            })
        }
    }
}

    data class VKUserData(
    val vkId: String,
    val email: String,
    val password: String,
    val name: String

)