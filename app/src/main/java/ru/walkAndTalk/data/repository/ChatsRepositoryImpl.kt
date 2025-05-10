package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.Postgrest
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository

class ChatsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
    private val eventsRepository: EventsRepository
) : ChatsRepository {

    override suspend fun fetchUserChats(): List<Chat> {
        // Предположим, что нам нужно получить чаты, связанные с текущим пользователем
        // Для простоты загрузим все чаты (нужна фильтрация по user_id через event_participants)
        val chats = supabaseWrapper.postgrest[Table.CHATS]
            .select()
            .decodeList<ChatDto>()

        return chats.mapNotNull { chatDto ->
            val event = eventsRepository.fetchEventById(chatDto.eventId)
            event?.let { chatDto.toDomain(it) }
        }
    }

    override suspend fun fetchChatById(id: String): Chat? {
        val chatDto = supabaseWrapper.postgrest[Table.CHATS]
            .select { filter { ChatDto::id eq id } }
            .decodeSingleOrNull<ChatDto>()

        return chatDto?.let { dto ->
            val event = eventsRepository.fetchEventById(dto.eventId)
            event?.let { dto.toDomain(it) }
        }
    }
}