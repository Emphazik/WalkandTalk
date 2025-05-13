package ru.walkAndTalk.data.repository

import android.net.Uri
import android.util.Log
import ru.walkAndTalk.data.mapper.fromDto
import ru.walkAndTalk.data.mapper.fromDtoList
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Bucket
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import ru.walkAndTalk.domain.repository.StorageRepository

class RemoteUsersRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
    private val storageRepository: StorageRepository
) : RemoteUsersRepository {

    override suspend fun fetchByEmail(email: String): User? {
        return supabaseWrapper.postgrest[Table.USERS]
            .select {
                filter { UserDto::email eq email }
            }
            .decodeSingleOrNull<UserDto>()
            ?.fromDto()
            .also { Log.d("RemoteUsersRepository", "Fetched user by email: $email, result: $it") }
    }

    override suspend fun fetchAll(): List<User> {
        return supabaseWrapper.postgrest[Table.USERS]
            .select()
            .decodeList<UserDto>()
            .fromDtoList()
            .also { Log.d("RemoteUsersRepository", "Fetched all users: ${it.size}") }
    }

    override suspend fun fetchById(id: String): User? {
        Log.d("RemoteUsersRepository", "Fetching user by id: $id")
        return supabaseWrapper.postgrest[Table.USERS]
            .select { filter { UserDto::id eq id } }
            .decodeSingleOrNull<UserDto>()
            ?.fromDto()
            .also { Log.d("RemoteUsersRepository", "Fetched user by id: $id, result: $it") }
    }

    override suspend fun fetchByVkId(id: Long): User? {
        Log.d("RemoteUsersRepository", "Fetching user by vkId: $id")
        return supabaseWrapper.postgrest[Table.USERS]
            .select { filter { UserDto::vkId eq id } }
            .decodeSingleOrNull<UserDto>()
            ?.fromDto()
            .also { Log.d("RemoteUsersRepository", "Fetched user by vkId: $id, result: $it") }
    }

    override suspend fun add(user: User) {
        Log.d("RemoteUsersRepository", "Adding user: id=${user.id}, email=${user.email}, phone=${user.phone}, vkId=${user.vkId}")
        try {
            supabaseWrapper.postgrest[Table.USERS].insert(user.toDto())
            Log.d("RemoteUsersRepository", "User added successfully: ${user.email}")
        } catch (e: Exception) {
            Log.e("RemoteUsersRepository", "Failed to add user: ${e.message}", e)
            throw e
        }
    }

    override suspend fun registerNewUser(vkUser: User): User {
        Log.d("RemoteUsersRepository", "Registering new user: ${vkUser.email}, vkId: ${vkUser.vkId}, id: ${vkUser.id}")
        add(vkUser)
        val createdUser = fetchByEmail(vkUser.email)
            ?: throw IllegalStateException("Failed to fetch created user: ${vkUser.email}")
        Log.d("RemoteUsersRepository", "New user registered with id: ${createdUser.id}")
        return createdUser
    }

    override suspend fun updateVKId(userId: String, vkId: Long) {
        Log.d("RemoteUsersRepository", "Updating vkId for userId: $userId, vkId: $vkId")
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("vk_id" to vkId)
            ) {
                filter { UserDto::id eq userId }
            }
            .also { Log.d("RemoteUsersRepository", "vkId updated for userId: $userId") }
    }

    override suspend fun searchUsers(query: String): List<User> {
//        return supabaseWrapper[Table.USERS]
//            .select {
//                filter {
//                    or(
//                        listOf(
//                            ilike("name", "%$query%"),
//                            ilike("bio", "%$query%")
//                        )
//                    )
//                }
//            }
//            .decodeList<UserDto>()
//            .map { it.toDomain() }
//    }
        return TODO("Provide the return value")
    }

    override suspend fun updateCityKnowledgeLevel(userId: String, levelId: String) {
        Log.d("RemoteUsersRepository", "Обновление city_knowledge_level_id для userId: $userId, levelId: $levelId")
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("city_knowledge_level_id" to levelId)
            ) {
                filter { UserDto::id eq userId }
            }
    }

    override suspend fun updateBio(userId: String, bio: String) {
        Log.d("RemoteUsersRepository", "Обновление bio для userId: $userId, bio: $bio")
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("bio" to bio)
            ) {
                filter { UserDto::id eq userId }
            }
    }

    override suspend fun updateGoals(userId: String, goals: String) {
        Log.d("RemoteUsersRepository", "Обновление goals для userId: $userId, goals: $goals")
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("goals" to goals)
            ) {
                filter { UserDto::id eq userId }
            }
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri, fileName: String) {
        Log.d("RemoteUsersRepository", "Загрузка изображения для userId: $userId, fileName: $fileName")
        storageRepository.upload(Bucket.PROFILE_IMAGES, fileName, imageUri)
    }

    override suspend fun getProfileImageUrl(userId: String, fileName: String): String {
        Log.d("RemoteUsersRepository", "Получение URL для userId: $userId, fileName: $fileName")
        return storageRepository.createSignedUrl(Bucket.PROFILE_IMAGES, fileName)
            //?: "https://tvecrsehuuqrjwjfgljf.supabase.co/storage/v1/object/sign/profile-images/default_profile.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InN0b3JhZ2UtdXJsLXNpZ25pbmcta2V5X2Y2YjA0NTBiLWVkNDktNGFkNi1iMGM2LWJiYzZmNzM0ZGY2YyJ9.eyJ1cmwiOiJwcm9maWxlLWltYWdlcy9kZWZhdWx0X3Byb2ZpbGUucG5nIiwiaWF0IjoxNzQ1NTI2MjM1LCJleHAiOjE3NzcwNjIyMzV9.RrxpUDm_OaKOOFFBICiPfVYgCdVTKMcyKqq6TKIYTv0"
    }

    override suspend fun updateProfileImageUrl(userId: String, imageUrl: String) {
        Log.d("RemoteUsersRepository", "Обновление profile_image_url для userId: $userId, url: $imageUrl")
        supabaseWrapper.postgrest[Table.USERS]
            .update(
                mapOf("profile_image_url" to imageUrl)
            ) {
                filter { UserDto::id eq userId }
            }
    }

    override suspend fun logout() {
        Log.d("RemoteUsersRepository", "Выполняется выход из аккаунта")
        supabaseWrapper.auth.signOut()
    }
}