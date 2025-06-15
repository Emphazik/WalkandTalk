package ru.walkAndTalk.data.repository

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.plugins.ClientRequestException
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.model.EventParticipantDto
import ru.walkAndTalk.data.model.EventStatusDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.EventParticipant
import ru.walkAndTalk.domain.repository.EventParticipantsRepository

class EventParticipantsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : EventParticipantsRepository {

    override suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            // Check current participant count and maxParticipants
            val event = supabaseWrapper.postgrest[Table.EVENTS]
                .select { filter { eq("id", eventId) } }
                .decodeSingleOrNull<EventDto>()
                ?: return Result.failure(Exception("Мероприятие не найдено"))

            if (event.maxParticipants != null) {
                val currentCount = getParticipantsCount(eventId)
                if (currentCount >= event.maxParticipants) {
                    return Result.failure(Exception("Мероприятие заполнено"))
                }
            }

            // Check if user is already participating
            val isParticipating = supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .select {
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<EventParticipantDto>()
                .isNotEmpty()

            if (isParticipating) {
                return Result.failure(Exception("Вы уже участвуете в этом мероприятии"))
            }

            // Insert participant
            val participant = EventParticipantDto(
                eventId = eventId,
                userId = userId,
                joinedAt = Clock.System.now().toString()
            )
            supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .insert(listOf(participant))
            Result.success(Unit)
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 409) {
                Result.failure(Exception("Вы уже участвуете в этом мероприятии"))
            } else {
                Result.failure(e)
            }
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

    override suspend fun isUserParticipating(eventId: String, userId: String): Boolean {
        return supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
            .select {
                filter {
                    eq("event_id", eventId)
                    eq("user_id", userId)
                }
            }
            .decodeList<EventParticipantDto>()
            .isNotEmpty()
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

    override suspend fun getStatusIdByName(name: String): String? {
        return try {
            val result = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
                .select {
                    filter { eq("name", name) }
                }
                .decodeSingle<EventStatusDto>()
            result.id
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun removeAllParticipants(eventId: String): Result<Unit> {
        return try {
            supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .delete {
                    filter { eq("event_id", eventId) }
                }
            Log.d("EventParticipantsRepository", "Все участники мероприятия $eventId удалены")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventParticipantsRepository", "Ошибка удаления участников для мероприятия $eventId: ${e.message}", e)
            Result.failure(e)
        }
    }
}
