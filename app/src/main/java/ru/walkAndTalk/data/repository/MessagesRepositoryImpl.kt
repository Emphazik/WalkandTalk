package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
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
        return try {
            val response = supabaseWrapper.postgrest.from(Table.MESSAGES)
                .select {
                    filter { eq("chat_id", chatId) }
                    order("created_at", Order.ASCENDING)
                }.decodeList<MessageDto>()
            val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id
            response.map { it.toDomain() }.filterNot { message ->
                message.deletedBy?.contains(currentUserId) == true
            }
        } catch (e: Exception) {
            println("MessagesRepository: Error fetching messages: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            throw e
        }
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
                filter { eq("id", messageId) }
            }
        println("MessagesRepository: Marked message as read: $messageId")
    }

    override suspend fun deleteMessages(messageIds: List<String>) {
        try {
            if (messageIds.isEmpty()) {
                println("MessagesRepository: No message IDs provided for deletion")
                return
            }

            messageIds.forEach { messageId ->
                supabaseWrapper.postgrest.from(Table.MESSAGES)
                    .delete {
                        filter { eq("id", messageId) }
                        filter { eq("sender_id", supabaseWrapper.auth.currentUserOrNull()?.id ?: "") } // Проверка RLS
                    }
                println("MessagesRepository: Successfully deleted message: $messageId")
            }
        } catch (e: Exception) {
            println("MessagesRepository: Error deleting messages: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun editMessage(messageId: String, newContent: String): Message {
        supabaseWrapper.postgrest.from(Table.MESSAGES)
            .update(mapOf("content" to newContent)) {
                filter { eq("id", messageId) }
            }
        val updatedMessage = supabaseWrapper.postgrest.from(Table.MESSAGES)
            .select { filter { eq("id", messageId) } }
            .decodeSingle<MessageDto>()
        println("MessagesRepository: Edited message id=$messageId, newContent=$newContent")
        return updatedMessage.toDomain()
    }
}