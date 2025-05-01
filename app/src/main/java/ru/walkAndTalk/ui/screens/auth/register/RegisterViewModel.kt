package ru.walkAndTalk.ui.screens.auth.register

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.walkAndTalk.data.network.SupabaseWrapper
import java.io.InputStream
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds


@Serializable
data class ProfileData(
    val user_id: String,
    val interests: List<String> = emptyList(),
    val goals: String = "",
    val city_knowledge_level: String = "новичок",
    val bio: String = ""
)

class RegisterViewModel(
    private val context: Context,
    private val supabaseWrapper: SupabaseWrapper
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterViewState())
    val state: StateFlow<RegisterViewState> = _state

    private val emailPattern: Pattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    fun onNameChange(newName: String) {
        _state.value = _state.value.copy(name = newName.trim())
    }

    fun onPhoneChange(newPhone: String) {
        _state.value = _state.value.copy(phone = newPhone.trim())
    }

    fun onEmailChange(newEmail: String) {
        _state.value = _state.value.copy(email = newEmail.trim())
    }

    fun onPasswordChange(newPassword: String) {
        _state.value = _state.value.copy(password = newPassword.trim())
    }

    fun onProfileImageSelected(uri: Uri) {
        _state.value = _state.value.copy(profileImageUri = uri)
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, isSuccess = false)

            try {
                // Валидация всех полей
                validateInputs()

                val client = supabaseWrapper.client

                // Выходим из текущей сессии, если она есть
                println("Register: Выход из любой существующей сессии...")
                client.auth.signOut()

                // Регистрация через email в Supabase Auth
                println("Register: Попытка зарегистрироваться с помощью электронной почты: ${_state.value.email}")
                var user = client.auth.signUpWith(Email) {
                    email = _state.value.email
                    password = _state.value.password
                }

                if (user == null) {
                    println("Register: signUpWith вернул null, при попытке войти в систему...")
                    client.auth.signInWith(Email) {
                        email = _state.value.email
                        password = _state.value.password
                    }
                    user = client.auth.currentUserOrNull()
                    println("Register: Sign in result: ${user?.id}")
                }

                if (user != null) {
                    println("Register: Пользователь успешно зарегистрирован, ID: ${user.id}")

                    println("Register: Проверка сессии...")
                    if (client.auth.currentSessionOrNull() == null) {
                        throw Exception("Сессия не устанавливается после регистрации или входа в систему.")
                    }
                    println("Register: Полученная сессия, текущий пользователь: ${client.auth.currentUserOrNull()?.id}")

                    // Загрузка фото в Storage или использование заглушки
                    var imageUrl: String? = null
                    _state.value.profileImageUri?.let { uri ->
                        println("Register: Загрузка изображения профиля...")
                        val fileName = "${user.id}/profile.jpg"
                        val fileBytes = uri.toByteArray(context)
                        client.storage.from("profile-images").upload(fileName, fileBytes) {
                            upsert = true
                        }
                        imageUrl = client.storage.from("profile-images").createSignedUrl(fileName, expiresIn = (60 * 60 * 24).seconds)
                        println("Register: Загруженное изображение, URL: $imageUrl")
                    } ?: run {
                        // Установка URL заглушки, если фото не выбрано
                        imageUrl = "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0"
                        println("Register: Используется заглушка для изображения профиля: $imageUrl")
                    }

                    // Сохранение данных в таблицу users
                    println("Register: Saving user data to 'users' table...")
                    client.postgrest["users"].insert(
                        mapOf(
                            "id" to user.id,
                            "email" to _state.value.email,
                            "phone" to _state.value.phone,
                            "name" to _state.value.name,
                            "profile_image_url" to imageUrl
                        )
                    )
                    println("Register: User data saved to 'users' table")

                    // Сохранение профиля
                    println("Register: Saving profile data to 'profiles' table...")
                    client.postgrest["profiles"].insert(
                        ProfileData(
                            user_id = user.id,
                            interests = emptyList(),
                            goals = "",
                            city_knowledge_level = "новичок",
                            bio = ""
                        )
                    )
                    println("Register: Profile data saved to 'profiles' table")

                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
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
                _state.value = _state.value.copy(isLoading = false, error = errorMessage, isSuccess = false)
            } catch (e: Exception) {
                println("Register: Error during registration: ${e.message}")
                _state.value = _state.value.copy(isLoading = false, error = e.message, isSuccess = false)
            }
        }
    }

    private fun validateInputs() {
        // Проверка имени
        if (_state.value.name.isBlank()) {
            throw Exception("Имя не должно быть пустым")
        }

        // Проверка телефона
        if (_state.value.phone.isBlank()) {
            throw Exception("Номер телефона не должен быть пустым")
        }
        if (!_state.value.phone.matches(Regex("^\\+7\\d{10}\$"))) {
            throw Exception("Номер телефона должен быть в формате +7 и содержать 10 цифр")
        }

        // Проверка email
        if (_state.value.email.isBlank()) {
            throw Exception("Электронная почта не должна быть пустой")
        }
        if (!emailPattern.matcher(_state.value.email).matches()) {
            throw Exception("Неверный формат электронной почты")
        }

        // Проверка пароля
        if (_state.value.password.length < 6) {
            throw Exception("Пароль должен состоять не менее чем из 6 символов")
        }
    }

    private fun Uri.toByteArray(context: Context): ByteArray {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(this)
        return inputStream?.use { it.readBytes() } ?: byteArrayOf()
    }
}
