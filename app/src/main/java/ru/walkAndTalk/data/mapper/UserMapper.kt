package ru.walkAndTalk.data.mapper

import com.vk.sdk.api.users.dto.UsersUserFullDto
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.domain.model.User

fun List<UserDto>.fromDtoList(): List<User> = map(UserDto::fromDto)

fun UserDto.fromDto(): User = User(
    id = id,
    email = email,
    phone = phone,
    name = name,
    profileImageUrl = profileImageUrl,
    vkId = vkId
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    phone = phone,
    name = name,
    profileImageUrl = profileImageUrl,
    vkId = vkId
)

fun UsersUserFullDto.fromVkUser(): User = User(
    id = "",
    email = email ?: "",
    phone = mobilePhone ?: "",
    name = "$lastName $firstName",
    profileImageUrl = photo50 ?: "",
    vkId = id.value,
)