package ru.walkAndTalk.data.mapper

import com.vk.sdk.api.users.dto.UsersUserFullDto
import kotlinx.datetime.Instant
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.domain.model.User

fun List<UserDto>.fromDtoList(): List<User> = map(UserDto::fromDto)

fun UserDto.fromDto(): User = User(
    id = id,
    email = email,
    phone = phone,
    name = name,
    profileImageUrl = profileImageUrl,
    vkId = vkId,
    interestIds = interestIds,
    cityKnowledgeLevelId = cityKnowledgeLevelId,
    bio = bio,
    goals = goals,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) }
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    phone = phone,
    name = name,
    profileImageUrl = profileImageUrl,
    vkId = vkId,
    interestIds = interestIds,
    cityKnowledgeLevelId = cityKnowledgeLevelId,
    bio = bio,
    goals = goals,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString()
)

fun UsersUserFullDto.fromVkUser(): User = User(
    id = "", // ID будет генерироваться позже
    email = email ?: "",
    phone = mobilePhone ?: "",
    name = "$lastName $firstName",
    profileImageUrl = photo50 ?: "",
    vkId = id.value,
    interestIds = emptyList(),
    cityKnowledgeLevelId = null,
    bio = null,
    goals = null,
    createdAt = Instant.fromEpochSeconds(0), // Заглушка, заменить на реальное время
    updatedAt = null
)