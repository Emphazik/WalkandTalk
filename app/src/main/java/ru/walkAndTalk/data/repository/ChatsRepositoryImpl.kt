package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.query.Order
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.time.OffsetDateTime
import java.util.UUID

class ChatsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
    private val eventsRepository: EventsRepository,
    private val usersRepository: RemoteUsersRepository
) : ChatsRepository {

    override suspend fun fetchUserChats(userId: String): List<Chat> {
        println("ChatsRepository: Fetching chats for userId=$userId")
        try {
            val chatIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                .select { filter { eq("user_id", userId) } }
                .decodeList<ChatParticipantDto>()
                .map { it.chatId }
            println("ChatsRepository: Found ${chatIds.size} chatIds: $chatIds")
            if (chatIds.isEmpty()) return emptyList()

            val chats = mutableListOf<ChatDto>()
            for (chatId in chatIds) {
                val chat = supabaseWrapper.postgrest[Table.CHATS]
                    .select { filter { eq("id", chatId) } }
                    .decodeSingleOrNull<ChatDto>()
                if (chat != null) {
                    chats.add(chat)
                }
                println("ChatsRepository: Fetched chatId=$chatId: $chat")
            }
            println("ChatsRepository: Found ${chats.size} chats: $chats")

            return chats.mapNotNull { chatDto ->
                val participantIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                    .select { filter { eq("chat_id", chatDto.id) } }
                    .decodeList<ChatParticipantDto>()
                    .map { it.userId }
                println("ChatsRepository: Participants for chatId=${chatDto.id}: $participantIds")

                // Извлекаем данные текущего пользователя
                val currentParticipant = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                    .select { filter { eq("chat_id", chatDto.id); eq("user_id", userId) } }
                    .decodeSingleOrNull<ChatParticipantDto>()
                println("ChatsRepository: Current participant for chatId=${chatDto.id}, userId=$userId: $currentParticipant")
                val isMuted = currentParticipant?.isMuted ?: false
                println("ChatsRepository: isMuted for chatId=${chatDto.id} and userId=$userId: $isMuted")

                val event = if (chatDto.type == "group" && chatDto.eventId != null) {
                    eventsRepository.fetchEventById(chatDto.eventId)
                } else null

                val participantId = participantIds.firstOrNull { it != userId }
                val participantUser = participantId?.let { usersRepository.fetchById(it) }

                val lastMessageQuery = supabaseWrapper.postgrest[Table.MESSAGES]
                    .select { filter { eq("chat_id", chatDto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                    .decodeSingleOrNull<MessageDto>()
                val lastMessageSenderId = lastMessageQuery?.senderId // Получаем senderId последнего сообщения

                chatDto.toDomain(
                    event = event,
                    participantName = participantUser?.name,
                    participantUser = participantUser,
                    participantIds = participantIds,
                    lastMessage = lastMessageQuery?.content,
                    lastMessageTime = lastMessageQuery?.createdAt,
                    lastMessageSenderId = lastMessageSenderId, // Передаем senderId
                    unreadCount = getUnreadCount(chatDto.id, userId),
                    isMessageRead = lastMessageQuery?.isRead,
                    isMuted = isMuted
                )
            }.also {
                println("ChatsRepository: Fetched ${it.size} chats for userId=$userId: $it")
            }
        } catch (e: Exception) {
            println("ChatsRepository: Error fetching chats: ${e.message}, stackTrace=${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun fetchChatById(id: String): Chat? {
        val chatDto = supabaseWrapper.postgrest.from(Table.CHATS)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<ChatDto>()

        return chatDto?.let { dto ->
            val participantIds = supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                .select { filter { eq("chat_id", dto.id) } }
                .decodeList<ChatParticipantDto>()
                .map { it.userId }
            val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id
            val currentParticipant = currentUserId?.let { userId ->
                supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                    .select { filter { eq("chat_id", dto.id); eq("user_id", userId) } }
                    .decodeSingleOrNull<ChatParticipantDto>()
            }
            val isMuted = currentParticipant?.isMuted ?: false

            val participantId = participantIds.firstOrNull { it != currentUserId }
            val participantUser = participantId?.let { usersRepository.fetchById(it) }

            val lastMessage = supabaseWrapper.postgrest.from(Table.MESSAGES)
                .select { filter { eq("chat_id", dto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                .decodeSingleOrNull<MessageDto>()

            dto.toDomain(
                event = if (dto.type == "group" && dto.eventId != null) eventsRepository.fetchEventById(dto.eventId) else null,
                participantName = participantUser?.name,
                participantUser = participantUser,
                participantIds = participantIds,
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.createdAt,
                unreadCount = currentUserId?.let { getUnreadCount(dto.id, it) } ?: 0,
                isMuted = isMuted
            )
        }.also {
            println("ChatsRepository: Fetched chat id=$id, result=$it")
        }
    }

    override suspend fun toggleMuteChat(chatId: String, mute: Boolean) {
        val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id
        if (currentUserId != null) {
            supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                .update(mapOf("is_muted" to mute)) {
                    filter { eq("chat_id", chatId); eq("user_id", currentUserId) }
                }
            println("ChatsRepository: Chat id=$chatId mute set to $mute for userId=$currentUserId")
        }
    }

    override suspend fun createGroupChat(eventId: String, userId: String): Chat {
        val event = eventsRepository.fetchEventById(eventId)
            ?: throw IllegalArgumentException("Event not found: $eventId")

        val participantIds = supabaseWrapper.postgrest.from(Table.EVENT_PARTICIPANTS)
            .select { filter { eq("event_id", eventId) } }
            .decodeList<ChatParticipantDto>()
            .map { it.userId }

        val chatDto = ChatDto(
            id = UUID.randomUUID().toString(),
            type = "group",
            eventId = eventId,
            createdAt = OffsetDateTime.now().toString(),
            updatedAt = OffsetDateTime.now().toString(),
//            isMuted = false
        )

        supabaseWrapper.postgrest.from(Table.CHATS).insert(chatDto)

        participantIds.forEach { participantId ->
            supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS).insert(
                ChatParticipantDto(
                    chatId = chatDto.id,
                    userId = participantId,
                    lastReadAt = null
                )
            )
        }

        val participantId = participantIds.firstOrNull { it != userId }
        val participantUser = participantId?.let { usersRepository.fetchById(it) }

        return chatDto.toDomain(
            event = event,
            participantName = participantUser?.name,
            participantUser = participantUser,
            participantIds = participantIds,
//            isMuted = chatDto.isMuted
        ).also {
            println("ChatsRepository: Created group chat id=${it.id} for eventId=$eventId")
        }
    }

    override suspend fun createPrivateChat(userId1: String, userId2: String): Chat {
        println("ChatsRepository: Creating private chat for userId1=$userId1, userId2=$userId2")
        val chatIds = supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
            .select { filter { eq("user_id", userId1) } }
            .decodeList<ChatParticipantDto>()
            .map { it.chatId }
        println("ChatsRepository: Found ${chatIds.size} chatIds for userId1=$userId1: $chatIds")

        var existingChat: ChatDto? = null
        for (chatId in chatIds) {
            val chat = supabaseWrapper.postgrest.from(Table.CHATS)
                .select { filter { eq("type", "private"); eq("id", chatId) } }
                .decodeSingleOrNull<ChatDto>()
            if (chat != null) {
                val participants = supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                    .select { filter { eq("chat_id", chat.id) } }
                    .decodeList<ChatParticipantDto>()
                    .map { it.userId }
                println("ChatsRepository: Checking participants for chatId=${chat.id}: $participants")
                if (participants.containsAll(listOf(userId1, userId2))) {
                    existingChat = chat
                    break
                }
            }
        }

        if (existingChat != null) {
            val participantUser = usersRepository.fetchById(userId2)
            val participantIds = supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                .select { filter { eq("chat_id", existingChat.id) } }
                .decodeList<ChatParticipantDto>()
                .map { it.userId }
            println("ChatsRepository: Found existing private chat id=${existingChat.id}, participantUser=$participantUser, participantIds=$participantIds")
            return existingChat.toDomain(
                participantName = participantUser?.name,
                participantUser = participantUser,
                participantIds = participantIds,
//                isMuted = existingChat.isMuted
            ).also {
                println("ChatsRepository: Returning existing private chat id=${it.id} for users $userId1, $userId2")
            }
        }

        val chatDto = ChatDto(
            id = UUID.randomUUID().toString(),
            type = "private",
            eventId = null,
            createdAt = OffsetDateTime.now().toString(),
            updatedAt = OffsetDateTime.now().toString(),
//            isMuted = false
        )
        println("ChatsRepository: Creating new chat: $chatDto")

        supabaseWrapper.postgrest.from(Table.CHATS).insert(chatDto)

        listOf(userId1, userId2).forEach { userId ->
            val participantDto = ChatParticipantDto(
                chatId = chatDto.id,
                userId = userId,
                lastReadAt = null
            )
            println("ChatsRepository: Adding participant to chatId=${chatDto.id}: $participantDto")
            supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS).insert(participantDto)
        }

        val participantUser = usersRepository.fetchById(userId2)
        return chatDto.toDomain(
            participantName = participantUser?.name,
            participantUser = participantUser,
            participantIds = listOf(userId1, userId2),
//            isMuted = chatDto.isMuted
        ).also {
            println("ChatsRepository: Created private chat id=${it.id} for users $userId1, $userId2")
        }
    }

    override suspend fun getUnreadCount(chatId: String, userId: String): Int {
        val lastReadAt = supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
            .select { filter { eq("chat_id", chatId); eq("user_id", userId) } }
            .decodeSingleOrNull<ChatParticipantDto>()
            ?.lastReadAt

        val messages = supabaseWrapper.postgrest.from(Table.MESSAGES)
            .select {
                filter {
                    eq("chat_id", chatId)
                    eq("is_read", false)
                    neq("sender_id", userId)
                    if (lastReadAt != null) {
                        gt("created_at", lastReadAt)
                    }
                }
            }
            .decodeList<MessageDto>()

        val unreadCount = messages.size
        return unreadCount.also {
            println("ChatsRepository: Unread count for chatId=$chatId, userId=$userId: $it")
        }
    }

    // Новые методы для контекстного меню

    override suspend fun markChatAsRead(chatId: String, userId: String) {
        val currentTime = OffsetDateTime.now().toString()
        supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
            .update(mapOf("last_read_at" to currentTime)) {
                filter { eq("chat_id", chatId); eq("user_id", userId) }
            }
        supabaseWrapper.postgrest.from(Table.MESSAGES)
            .update(mapOf("is_read" to true)) {
                filter { eq("chat_id", chatId); neq("sender_id", userId) }
            }
        println("ChatsRepository: Marked chat id=$chatId as read for userId=$userId")
    }

    override suspend fun deleteChat(chatId: String) {
        try {
            // Удаляем сообщения
            supabaseWrapper.postgrest.from(Table.MESSAGES)
                .delete {
                    filter { eq("chat_id", chatId) }
                }
            // Удаляем участников чата
            supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                .delete {
                    filter { eq("chat_id", chatId) }
                }
            // Удаляем сам чат
            supabaseWrapper.postgrest.from(Table.CHATS)
                .delete {
                    filter { eq("id", chatId) }
                }
            println("ChatsRepository: Deleted chatId=$chatId")
        } catch (e: Exception) {
            println("ChatsRepository: Error deleting chat: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun clearChatHistory(chatId: String, userId: String) {
        try {
            val messages = supabaseWrapper.postgrest.from(Table.MESSAGES)
                .select {
                    filter { eq("chat_id", chatId) }
                }.decodeList<MessageDto>()

            messages.forEach { message ->
                val updatedDeletedBy = (message.deletedBy ?: emptyList()).toMutableList().apply {
                    if (!contains(userId)) add(userId)
                }
                supabaseWrapper.postgrest.from(Table.MESSAGES)
                    .update(
                        mapOf("deleted_by" to updatedDeletedBy)
                    ) {
                        filter { eq("id", message.id) }
                    }
            }
            println("ChatsRepository: Cleared history for chatId=$chatId for userId=$userId")
        } catch (e: Exception) {
            println("ChatsRepository: Error clearing history: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun fetchUsersForChats(chatIds: List<String>): List<User> {
        return emptyList()
    }
}