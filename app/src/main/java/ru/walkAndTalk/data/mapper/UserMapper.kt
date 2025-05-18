package ru.walkAndTalk.data.mapper

import android.util.Log
import com.vk.id.VKIDUser
import com.vk.sdk.api.users.dto.UsersUserFullDto
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.domain.model.User
import java.util.UUID
import kotlin.String

fun List<UserDto>.fromDtoList(): List<User> = map(UserDto::fromDto)

fun UserDto.fromDto(): User = User(
    id = id,
    email = email,
    password = password,
    phone = phone,
    name = name,
    profileImageUrl = profileImageUrl,
    vkId = vkId,
    interestIds = interestIds,
    cityKnowledgeLevelId = cityKnowledgeLevelId,
    bio = bio,
    goals = goals,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) },
    birthdate = birthdate
)
fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    password = password,
    phone = phone,
    name = name,
    profileImageUrl = profileImageUrl,
    vkId = vkId,
    interestIds = interestIds,
    cityKnowledgeLevelId = cityKnowledgeLevelId,
    bio = bio,
    goals = goals,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt?.toString(),
    birthdate = birthdate
)

fun VKIDUser.toUser(
    vkId: Long
): User = User(
    id = "", // id будет установлен после регистрации через Supabase Auth
    email = email.toString(),
    password = UUID.randomUUID().toString(),
    phone = "+$phone",
    name = firstName,
    profileImageUrl = photo200 ?: "",
    vkId = vkId,
    interestIds = emptyList(),
    cityKnowledgeLevelId = null,
    bio = null,
    goals = null,
    createdAt = System.now(),
    updatedAt = System.now(),
    birthdate = null
)

fun UsersUserFullDto.fromVkUser(): User {
    Log.d("VKUsersRepository", "Raw VK user data: " +
            "id=${id.value}, " +
            "firstName=$firstName, " +
            "lastName=$lastName, " +
            "email=$email, " +
            "phone=$mobilePhone, " +
            "photo50=$photo50," +
            "birthdate=$bdate"
    )

    return User(
        id = "", // id будет установлен после регистрации через Supabase Auth
        email = email.toString(),
        password = UUID.randomUUID().toString(),
        phone = mobilePhone.toString(),
        name = "$lastName $firstName",
        profileImageUrl = photo200 ?: "",
        vkId = id.value,
        interestIds = emptyList(),
        cityKnowledgeLevelId = null,
        bio = null,
        goals = null,
        createdAt = System.now(),
        updatedAt = System.now(),
        birthdate = bdate
    )
}