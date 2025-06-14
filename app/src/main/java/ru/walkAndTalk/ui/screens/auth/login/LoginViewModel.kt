package ru.walkAndTalk.ui.screens.auth.login

import android.util.Log
import com.vk.api.sdk.VK
import com.vk.dto.common.id.toUserId
import com.vk.id.AccessToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.RestException
import org.orbitmvi.orbit.annotation.OrbitExperimental
import ru.walkAndTalk.data.mapper.toUser
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Regex
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.VKUsersRepository
import ru.walkAndTalk.ui.orbit.ContainerViewModel

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

//    fun onLoginClick() = intent {
//        reduce { state.copy(isLoading = true, error = null) }
//        try {
//            validateInputs()
//            var email = state.email
//            if (state.email.matches(Regex.PHONE)) {
//
//                val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
//                    filter { eq("phone", state.email) }
//                }.decodeSingleOrNull<UserInfo>()
//                email = userData?.email ?: throw Exception("Телефон не зарегистрирован")
//            }
//
//            supabaseWrapper.auth.signInWith(Email) {
//                this.email = email
//                this.password = state.password
//            }
//
//            val user = supabaseWrapper.auth.currentUserOrNull()
//            if (user != null) {
//                if (supabaseWrapper.auth.currentSessionOrNull() == null) {
//                    throw Exception("Сессия не установлена после входа.")
//                }
//                val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
//                    filter { eq("id", user.id) }
//                }.decodeSingle<UserDto>()
//                reduce { state.copy(isLoading = false, user = null) }
//                if (userData.isAdmin) {
//                    postSideEffect(LoginSideEffect.OnNavigateAdmin(user.id))
//                } else {
//                    postSideEffect(LoginSideEffect.OnNavigateMain(user.id))
//                }
//            } else {
//                throw Exception("Не удалось войти: пользователь не найден.")
//            }
//        } catch (e: RestException) {
//
//            val errorMessage = when {
//                e.message?.contains("invalid_grant") == true -> "Неверный email/телефон или пароль."
//                e.message?.contains("email_not_confirmed") == true -> "Email не подтвержден. Проверьте почту."
//                else -> "Произошла ошибка при входе."
//            }
//            reduce { state.copy(isLoading = false, error = errorMessage) }
//        } catch (e: Exception) {
//            reduce {
//                state.copy(
//                    isLoading = false,
//                    error = e.message ?: "Произошла ошибка при входе."
//                )
//            }
//        }
//    }
    fun onLoginClick() = intent {
        // Лог начала выполнения функции
        Log.d("LoginViewModel", "onLoginClick: Начало процесса входа")

        reduce { state.copy(isLoading = true, error = null) }
        try {
            // Лог валидации входных данных
            Log.d("LoginViewModel", "onLoginClick: Выполняется валидация введённых данных")
            validateInputs()

            var email = state.email
            if (state.email.matches(Regex.PHONE)) {
                // Лог проверки телефона
                Log.d("LoginViewModel", "onLoginClick: Ввод распознан как телефон: ${state.email}")
                val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
                    filter { eq("phone", state.email) }
                }.decodeSingleOrNull<UserInfo>()
                email = userData?.email ?: throw Exception("Телефон не зарегистрирован")
                // Лог успешного получения email
                Log.d("LoginViewModel", "onLoginClick: Получен email для телефона: $email")
            }

            // Лог попытки входа через Supabase Auth
            Log.d("LoginViewModel", "onLoginClick: Попытка входа с email: $email")
            supabaseWrapper.auth.signInWith(Email) {
                this.email = email
                this.password = state.password
            }

            val user = supabaseWrapper.auth.currentUserOrNull()
            if (user != null) {
                // Лог проверки сессии
                Log.d("LoginViewModel", "onLoginClick: Пользователь найден, проверка сессии")
                if (supabaseWrapper.auth.currentSessionOrNull() == null) {
                    throw Exception("Сессия не установлена после входа.")
                }
                val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
                    filter { eq("id", user.id) }
                }.decodeSingle<UserDto>()
                // Лог успешной загрузки данных пользователя
                Log.d("LoginViewModel", "onLoginClick: Данные пользователя загружены, isAdmin: ${userData.isAdmin}")
                reduce { state.copy(isLoading = false, user = null) }
                if (userData.isAdmin) {
                    postSideEffect(LoginSideEffect.OnNavigateAdmin(user.id))
                    // Лог перехода на админскую панель
                    Log.d("LoginViewModel", "onLoginClick: Переход на админскую панель для userId: ${user.id}")
                } else {
                    postSideEffect(LoginSideEffect.OnNavigateMain(user.id))
                    // Лог перехода на главный экран
                    Log.d("LoginViewModel", "onLoginClick: Переход на главный экран для userId: ${user.id}")
                }
            } else {
                throw Exception("Не удалось войти: пользователь не найден.")
            }
        } catch (e: RestException) {
            // Лог ошибки RestException
            Log.e("LoginViewModel", "onLoginClick: Ошибка RestException: ${e.message}")
            val errorMessage = when {
                e.message?.contains("invalid_grant") == true -> "Неверный email/телефон или пароль."
                e.message?.contains("email_not_confirmed") == true -> "Email не подтвержден. Проверьте почту."
                else -> "Произошла ошибка при входе."
            }
            reduce { state.copy(isLoading = false, error = errorMessage) }
        } catch (e: Exception) {
            // Лог общей ошибки
            Log.e("LoginViewModel", "onLoginClick: Общая ошибка: ${e.message}")
            reduce {
                state.copy(
                    isLoading = false,
                    error = "Произошла ошибка при входе. Попробуйте ещё раз или позже."
//                    error = e.message ?: "Произошла ошибка при входе."
                )
            }
        }
    }

    fun onVKAuth(accessToken: AccessToken) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            VK.saveAccessToken(
                userId = accessToken.userID.toUserId(),
                accessToken = accessToken.token,
                secret = accessToken.idToken,
                expiresInSec = ((System.currentTimeMillis() - accessToken.expireTime) / 1000).toInt(),
                createdMs = System.currentTimeMillis(),
            )
            Log.d("LoginViewModel", "Saved accessToken: ${accessToken.userID}")
            val vkUser = accessToken.userData.toUser(accessToken.userID)
            Log.d("LoginViewModel", "Starting VK auth with accessToken: ${accessToken.userID}")

            // Получаем данные пользователя из VK API
//            val vkUser = vkUsersRepository.fetchUser(accessToken.userID)
            Log.d("LoginViewModel", "Fetched VK user: vkId=${vkUser.vkId}, email=${vkUser.email}, phone=${vkUser.phone}")

            // Проверяем, есть ли пользователь в базе по почте
            var existingUser = remoteUsersRepository.fetchByEmail(vkUser.email)
            val userId = if (existingUser != null) {
                supabaseWrapper.auth.signInWith(Email) {
                    this.email = existingUser.email
                    this.password = existingUser.password
                }
                remoteUsersRepository.updateVKId(existingUser.id, accessToken.userID)
                existingUser.id
            } else {
                // Проверяем, есть ли пользователь в базе по vkId
                existingUser = remoteUsersRepository.fetchByVkId(accessToken.userID)
                if (existingUser != null) {
                    Log.d("LoginViewModel", "User found with vkId: ${vkUser.vkId}")
                    supabaseWrapper.auth.signInWith(Email) {
                        this.email = existingUser.email
                        this.password = existingUser.password
                    }
                    existingUser.id
                } else {
                    // Пользователь не найден, регистрируем нового через Supabase Auth
                    Log.d("LoginViewModel", "Signing up with email: ${vkUser.email}")

                    supabaseWrapper.auth.signUpWith(Email) {
                        this.email = vkUser.email
                        this.password = vkUser.password
                    }

                    // Получаем userId из ответа Supabase Auth
                    val userId = supabaseWrapper.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Failed to get userId from Supabase Auth")
                    Log.d("LoginViewModel", "Supabase Auth signup successful, userId: $userId")

                    // Создаем объект User с id из Supabase Auth
                    val newUser = vkUser.copy(id = userId)
                    // Сохраняем пользователя в таблицу users
                    remoteUsersRepository.add(newUser)
                    Log.d("LoginViewModel", "New user added to users table with id: $userId")
                    userId
                }
            }
            // Проверяем роль пользователя
            val userData = supabaseWrapper.postgrest.from(Table.USERS).select {
                filter { eq("id", userId) }
            }.decodeSingle<UserDto>()
            reduce { state.copy(isLoading = false) }
            if (userData.isAdmin) {
                postSideEffect(LoginSideEffect.OnNavigateAdmin(userId))
            } else {
                postSideEffect(LoginSideEffect.OnNavigateMain(userId))
            }
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
