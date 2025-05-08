package ru.walkAndTalk.data.usecase

import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.domain.repository.AuthRepository
import ru.walkAndTalk.domain.repository.LocalDataStoreRepository

class AuthenticateUserUseCase(
    private val authRepository: AuthRepository,
    private val localDataStoreRepository: LocalDataStoreRepository
) {
    suspend operator fun invoke(
        accessToken: String,
        vkId: Long,
        email: String,
        phone: String,
        name: String
    ): UserDto {
        localDataStoreRepository.saveAccessToken(accessToken)
        return authRepository.authenticateUser(vkId, email, phone, name)
    }
}