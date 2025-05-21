package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.Serializable
import ru.walkAndTalk.data.model.EventParticipantDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.EventParticipant
import ru.walkAndTalk.domain.repository.EventParticipantsRepository

class EventParticipantsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : EventParticipantsRepository {

    override suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val participant = EventParticipantDto(eventId = eventId, userId = userId)
            supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .insert(listOf(participant)) // Оборачиваем в список
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 409) { // Конфликт (например, пользователь уже участвует)
                Result.failure(Exception("Вы уже участвуете в этом мероприятии"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUserParticipating(eventId: String, userId: String): Boolean {
        return try {
            val result = supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .select(Columns.list("event_id", "user_id", "joined_at")) { // Запрашиваем все поля
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<EventParticipantDto>()
            result.isNotEmpty()
        } catch (e: Exception) {
            println("Ошибка при проверке участия: ${e.message}")
            false
        }
    }

    override suspend fun leaveEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .delete {
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getParticipantsCount(eventId: String): Int {
        return supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
            .select {
                filter { eq("event_id", eventId) }
            }
            .decodeList<EventParticipantDto>()
            .size
    }
}
