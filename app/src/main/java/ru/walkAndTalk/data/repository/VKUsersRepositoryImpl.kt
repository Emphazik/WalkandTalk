package ru.walkAndTalk.data.repository

import com.vk.api.sdk.VK
import com.vk.dto.common.id.toUserId
import com.vk.id.VKID
import com.vk.sdk.api.users.UsersService
import ru.walkAndTalk.data.mapper.fromVkUser
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.VKUsersRepository

class VKUsersRepositoryImpl : VKUsersRepository {

    override suspend fun fetchUser(id: Long): User {
        val users = VK.executeSync(
            UsersService()
                .usersGet(
                    userIds = listOf(id.toUserId()),
                )
                .addParam("access_token", VKID.instance.accessToken?.token)
        )
        return users.first().fromVkUser()
    }

}