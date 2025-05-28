package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.query.Order
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.domain.repository.MessagesRepository
import java.time.OffsetDateTime
import java.util.UUID

class MessagesRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : MessagesRepository {
    override suspend fun fetchMessages(chatId: String): List<Message> {
        return supabaseWrapper.postgrest.from(Table.MESSAGES)
            .select { filter { eq("chat_id", chatId) }; order("created_at", Order.ASCENDING) }
            .decodeList<MessageDto>()
            .map { it.toDomain() }
            .also { println("MessagesRepository: Fetched ${it.size} messages for chatId=$chatId") }
    }

    override suspend fun sendMessage(chatId: String, senderId: String, content: String): Message {
        val messageDto = MessageDto(
            id = UUID.randomUUID().toString(), // Локальный ID как запасной вариант
            chatId = chatId,
            senderId = senderId,
            content = content,
            createdAt = OffsetDateTime.now().toString(),
            isRead = false
        )
        val response = supabaseWrapper.postgrest.from(Table.MESSAGES)
            .insert(messageDto)
        val lastMessage = supabaseWrapper.postgrest.from(Table.MESSAGES)
            .select { filter { eq("chat_id", chatId) }; order("created_at", Order.DESCENDING); limit(1) }
            .decodeSingle<MessageDto>()
        return lastMessage.toDomain()
        println("MessagesRepository: Sent message to chatId=$chatId: $response")
    }

    override suspend fun markMessageAsRead(messageId: String) {
        supabaseWrapper.postgrest.from(Table.MESSAGES)
            .update(mapOf("is_read" to true)) {
                filter { eq("id", messageId) } // Правильный синтаксис для фильтрации
            }
        println("MessagesRepository: Marked message as read: $messageId")
    }
}