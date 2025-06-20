package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.MessagesRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.time.OffsetDateTime
import java.util.UUID

class MessagesRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
    private val chatsRepository: ChatsRepository,
    private val usersRepository: RemoteUsersRepository
) : MessagesRepository {

    override suspend fun fetchMessages(chatId: String): List<Message> {
        println("MessagesRepository: Fetching messages for chatId=$chatId")
        val chat = chatsRepository.fetchChatById(chatId)
            ?: throw IllegalArgumentException("Chat not found: $chatId")
        val isGroupChat = chat.type == "group"
        val messages = supabaseWrapper.postgrest[Table.MESSAGES]
            .select {
                filter { eq("chat_id", chatId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<MessageDto>()
        return messages.map { messageDto ->
            val senderName = if (isGroupChat && messageDto.senderId != supabaseWrapper.auth.currentUserOrNull()?.id) {
                usersRepository.fetchById(messageDto.senderId)?.name ?: "Unknown"
            } else {
                null
            }
            messageDto.toDomain(senderName)
        }.also {
            println("MessagesRepository: Fetched ${it.size} messages for chatId=$chatId")
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
            val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id ?: return
            messageIds.forEach { messageId ->
                supabaseWrapper.postgrest.from(Table.MESSAGES)
                    .delete {
                        filter { eq("id", messageId) }
                        filter { eq("sender_id", currentUserId) }
                    }
                println("MessagesRepository: Successfully deleted message: $messageId")
            }
        } catch (e: Exception) {
            println("MessagesRepository: Error deleting messages: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            throw e
        }
    }

//    override suspend fun deleteMessages(messageIds: List<String>) {
//        try {
//            if (messageIds.isEmpty()) {
//                println("MessagesRepository: No message IDs provided for deletion")
//                return
//            }
//
//            messageIds.forEach { messageId ->
//                supabaseWrapper.postgrest.from(Table.MESSAGES)
//                    .delete {
//                        filter { eq("id", messageId) }
//                        filter { eq("sender_id", supabaseWrapper.auth.currentUserOrNull()?.id ?: "") } // Проверка RLS
//                    }
//                println("MessagesRepository: Successfully deleted message: $messageId")
//            }
//        } catch (e: Exception) {
//            println("MessagesRepository: Error deleting messages: ${e.message}, stacktrace: ${e.stackTraceToString()}")
//            throw e
//        }
//    }

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

    override suspend fun getMessageById(messageId: String): Message? {
        return try {
            val response = supabaseWrapper.postgrest.from("messages")
                .select { filter { eq("id", messageId) } }
                .decodeSingleOrNull<Map<String, JsonElement>>()
            response?.let { record ->
                Message(
                    id = record["id"]?.jsonPrimitive?.content ?: return null,
                    chatId = record["chat_id"]?.jsonPrimitive?.content ?: return null,
                    senderId = record["sender_id"]?.jsonPrimitive?.content ?: return null,
                    content = record["content"]?.jsonPrimitive?.content ?: return null,
                    createdAt = record["created_at"]?.jsonPrimitive?.content ?: return null,
                    isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false,
                    tempId = null,
                    deletedBy = record["deleted_by"]?.let { element ->
                        if (element is JsonArray) element.mapNotNull { it.jsonPrimitive.contentOrNull } else emptyList()
                    } ?: emptyList(),
                    senderName = null
                )
            }
        } catch (e: Exception) {
            println("MessagesRepository: Error fetching message $messageId: ${e.message}")
            null
        }
    }

    override suspend fun getMessagesByChatId(chatId: String): List<Message> {
        return try {
            val response = supabaseWrapper.postgrest.from("messages")
                .select { filter { eq("chat_id", chatId) } }
                .decodeList<Map<String, JsonElement>>()
            response.mapNotNull { record ->
                val messageId = record["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val senderId = record["sender_id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val content = record["content"]?.jsonPrimitive?.content ?: return@mapNotNull null
                Message(
                    id = messageId,
                    chatId = record["chat_id"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                    senderId = senderId,
                    content = content,
                    createdAt = record["created_at"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                    isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false,
                    tempId = null,
                    deletedBy = record["deleted_by"]?.let { element ->
                        if (element is JsonArray) element.mapNotNull { it.jsonPrimitive.contentOrNull } else emptyList()
                    } ?: emptyList(),
                    senderName = null // Будет заполнено в ChatViewModel, если нужно
                )
            }
        } catch (e: Exception) {
            println("MessagesRepository: Error fetching messages for chatId=$chatId: ${e.message}")
            emptyList()
        }
    }
}