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

    override suspend fun sendMessage(chatId: String, senderId: String, content: String) {
        val messageDto = MessageDto(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            content = content,
            createdAt = OffsetDateTime.now().toString(),
            isRead = false
        )
        supabaseWrapper.postgrest.from(Table.MESSAGES).insert(messageDto)
        println("MessagesRepository: Sent message to chatId=$chatId: $messageDto")
    }
}