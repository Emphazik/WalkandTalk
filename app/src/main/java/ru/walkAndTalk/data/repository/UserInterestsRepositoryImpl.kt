package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.InterestDto
import ru.walkAndTalk.data.model.UserDto
import ru.walkAndTalk.data.model.UserInterestDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Interest
import ru.walkAndTalk.domain.repository.UserInterestsRepository
import java.util.Locale.filter

class UserInterestsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : UserInterestsRepository {

    override suspend fun fetchInterestsForUser(userId: String): List<Interest> {
        return supabaseWrapper.postgrest[Table.USER_INTERESTS]
            .select {
                filter { UserInterestDto::userId eq userId }
            }
            .decodeList<UserInterestDto>()
            .mapNotNull { userInterest ->
                supabaseWrapper.postgrest[Table.INTERESTS]
                    .select { filter { InterestDto::id eq userInterest.interestId } }
                    .decodeSingleOrNull<InterestDto>()
                    ?.toDomain()
            }
    }

    override suspend fun addInterest(userId: String, interestId: String) {
        // Добавляем связь в таблицу user_interests
        supabaseWrapper.postgrest[Table.USER_INTERESTS]
            .insert(
                mapOf("user_id" to userId, "interest_id" to interestId)
            )

        // Обновляем interest_ids в таблице users
        val currentUser = supabaseWrapper.postgrest[Table.USERS]
            .select { filter { UserDto::id eq userId } }
            .decodeSingleOrNull<UserDto>()

        if (currentUser != null) {
            val updatedInterestIds = (currentUser.interestIds + interestId).distinct()
            supabaseWrapper.postgrest[Table.USERS]
                .update(
                    mapOf("interest_ids" to updatedInterestIds)
                ) {
                    filter { UserDto::id eq userId }
                }
        }
    }

    override suspend fun removeInterest(userId: String, interestId: String) {
        // Удаляем связь из таблицы user_interests
        supabaseWrapper.postgrest[Table.USER_INTERESTS]
            .delete {
                filter {
                    UserInterestDto::userId eq userId
                    UserInterestDto::interestId eq interestId
                }
            }

        // Обновляем interest_ids в таблице users
        val currentUser = supabaseWrapper.postgrest[Table.USERS]
            .select { filter { UserDto::id eq userId } }
            .decodeSingleOrNull<UserDto>()

        if (currentUser != null) {
            val updatedInterestIds = currentUser.interestIds - interestId
            supabaseWrapper.postgrest[Table.USERS]
                .update(
                    mapOf("interest_ids" to updatedInterestIds)
                ) {
                    filter { UserDto::id eq userId }
                }
        }
    }

}