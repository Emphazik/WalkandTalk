package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.query.Columns.Companion.raw
import io.github.jan.supabase.postgrest.query.Order
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.data.model.ChatWithDetailsDto
import ru.walkAndTalk.data.model.EventParticipantDto
import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.time.OffsetDateTime
import java.util.UUID
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.coroutines.coroutineScope
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.walkAndTalk.data.mapper.fromDto

class ChatsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper,
    private val eventsRepository: EventsRepository,
    private val usersRepository: RemoteUsersRepository
) : ChatsRepository {

//    override suspend fun fetchUserChats(userId: String): List<Chat> {
//        println("ChatsRepository: Fetching chats for userId=$userId")
//        try {
//            val chatIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
//                .select { filter { eq("user_id", userId) } }
//                .decodeList<ChatParticipantDto>()
//                .map { it.chatId }
//                .distinct()
//            println("ChatsRepository: Found ${chatIds.size} chatIds: $chatIds")
//            if (chatIds.isEmpty()) return emptyList()
//
//            val chats = mutableListOf<ChatDto>()
//            for (chatId in chatIds) {
//                val chat = supabaseWrapper.postgrest[Table.CHATS]
//                    .select { filter { eq("id", chatId) } }
//                    .decodeSingleOrNull<ChatDto>()
//                if (chat != null) {
//                    chats.add(chat)
//                }
//                println("ChatsRepository: Fetched chatId=$chatId: $chat")
//            }
//            println("ChatsRepository: Found ${chats.size} chats")
//
//            return chats.mapNotNull { chatDto ->
//                val participantIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
//                    .select { filter { eq("chat_id", chatDto.id) } }
//                    .decodeList<ChatParticipantDto>()
//                    .map { it.userId }
//                println("ChatsRepository: Participants for chatId=${chatDto.id}: $participantIds")
//
//                val currentParticipant = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
//                    .select { filter { eq("chat_id", chatDto.id); eq("user_id", userId) } }
//                    .decodeSingleOrNull<ChatParticipantDto>()
//                println("ChatsRepository: Current participant for chatId=${chatDto.id}, userId=$userId: $currentParticipant")
//                val isMuted = currentParticipant?.isMuted ?: false
//                println("ChatsRepository: isMuted for chatId=${chatDto.id} and userId=$userId: $isMuted")
//
//                val event = if (chatDto.type == "group" && chatDto.eventId != null) {
//                    eventsRepository.fetchEventById(chatDto.eventId)
//                } else null
//
//                // Для личных чатов выбираем другого участника
//                val (participantName, participantUser) = if (chatDto.type == "private") {
//                    val participantId = participantIds.firstOrNull { it != userId }
//                    val user = participantId?.let { usersRepository.fetchById(it) }
//                    user?.name to user
//                } else {
//                    null to null // Для групповых чатов не заполняем
//                }
//
//
//                val lastMessageQuery = supabaseWrapper.postgrest[Table.MESSAGES]
//                    .select { filter { eq("chat_id", chatDto.id) }; order("created_at", Order.DESCENDING); limit(1) }
//                    .decodeSingleOrNull<MessageDto>()
//                val lastMessageSenderId = lastMessageQuery?.senderId
//
//                val senderName = if (chatDto.type == "group" && lastMessageSenderId != null && lastMessageSenderId != userId) {
//                    usersRepository.fetchById(lastMessageSenderId)?.name ?: "Неизвестный"
//                } else {
//                    null
//                }
//                chatDto.toDomain(
//                    event = event,
////                    participantName = participantName,
//                    participantName = if (chatDto.type == "private") participantName else senderName,
//                    participantUser = participantUser,
//                    participantIds = participantIds,
//                    lastMessage = lastMessageQuery?.content,
//                    lastMessageTime = lastMessageQuery?.createdAt,
//                    lastMessageSenderId = lastMessageSenderId,
//                    unreadCount = getUnreadCount(chatDto.id, userId),
//                    isMessageRead = lastMessageQuery?.isRead,
//                    isMuted = isMuted
//                )
//            }.also {
//                println("ChatsRepository: Fetched ${it.size} chats for userId=$userId: $it")
//            }
//        } catch (e: Exception) {
//            println("ChatsRepository: Error fetching chats: ${e.message}, stackTrace=${e.stackTraceToString()}")
//            throw e
//        }
//    }

    override suspend fun fetchUserChats(userId: String): List<Chat> = coroutineScope {
        println("ChatsRepository: Fetching chats for userId=$userId")
        try {
            val chatsData = supabaseWrapper.postgrest.rpc(
                function = "fetch_user_chats",
                parameters = buildJsonObject { put("input_user_id", userId) }
            ).decodeList<ChatWithDetailsDto>()
            println("ChatsRepository: Fetched ${chatsData.size} chats with details")

            val eventIds = chatsData
                .filter { it.chat.type == "group" && it.chat.eventId != null }
                .map { it.chat.eventId!! }
                .distinct()

            val eventDeferred = eventIds.map { eventId ->
                async { eventsRepository.fetchEventById(eventId) }
            }
            val events = eventDeferred.awaitAll()
            val eventMap = eventIds.zip(events).filter { it.second != null }.associate { it.first to it.second }

            chatsData.mapNotNull { chatWithDetails ->
                val chatDto = chatWithDetails.chat
                val participantIds = chatWithDetails.participants.map { it.userId }
                val isMuted = chatWithDetails.currentParticipant?.isMuted ?: false
                val event = if (chatDto.type == "group" && chatDto.eventId != null) {
                    eventMap[chatDto.eventId]
                } else null
                val (participantName, participantUser) = if (chatDto.type == "private") {
                    val participantId = participantIds.firstOrNull { it != userId }
                    val userDto = chatWithDetails.users.find { it.id == participantId }
                    (userDto?.name ?: "Неизвестный пользователь") to userDto?.fromDto()
                } else {
                    null to null
                }
                chatsData.forEach { chatWithDetails ->
                    println("Chat ID: ${chatWithDetails.chat.id}, Last Message: ${chatWithDetails.lastMessage?.content}, Sender ID: ${chatWithDetails.lastMessage?.senderId}")
                }

                val lastMessageQuery = supabaseWrapper.postgrest[Table.MESSAGES]
                    .select { filter { eq("chat_id", chatDto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                    .decodeSingleOrNull<MessageDto>()
                val lastMessageSenderId = lastMessageQuery?.senderId

//                val lastMessage = chatWithDetails.lastMessage
                val senderName = if (chatDto.type == "group" && lastMessageQuery?.senderId != null && lastMessageQuery.senderId != userId) {
                    chatWithDetails.users.find { it.id == lastMessageQuery.senderId }?.name ?: "Неизвестный"
                } else {
                    null
                }
                chatDto.toDomain(
                    event = event,
                    participantName = if (chatDto.type == "private") participantName else senderName,
                    participantUser = participantUser,
                    participantIds = participantIds,
                    lastMessage = lastMessageQuery?.content,
                    lastMessageTime = lastMessageQuery?.createdAt,
                    lastMessageSenderId = lastMessageSenderId,
                    unreadCount = getUnreadCount(chatDto.id, userId),
                    isMessageRead = lastMessageQuery?.isRead,
                    isMuted = isMuted
                )
            }.also {
                println("ChatsRepository: Mapped ${it.size} chats for userId=$userId")
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

            // Для личных чатов выбираем другого участника
            val (participantName, participantUser) = if (dto.type == "private") {
                val participantId = participantIds.firstOrNull { it != currentUserId }
                val user = participantId?.let { usersRepository.fetchById(it) }
                user?.name to user
            } else {
                null to null // Для групповых чатов не заполняем
            }

            val lastMessage = supabaseWrapper.postgrest.from(Table.MESSAGES)
                .select { filter { eq("chat_id", dto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                .decodeSingleOrNull<MessageDto>()

            dto.toDomain(
                event = if (dto.type == "group" && dto.eventId != null) eventsRepository.fetchEventById(dto.eventId) else null,
                participantName = participantName,
                participantUser = participantUser,
                participantIds = participantIds,
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.createdAt,
                lastMessageSenderId = lastMessage?.senderId,
                unreadCount = currentUserId?.let { getUnreadCount(dto.id, it) } ?: 0,
                isMuted = isMuted
            )
        }.also {
            println("ChatsRepository: Fetched chat id=$id, result=$it")
        }
    }

    override suspend fun findOrCreatePrivateChat(userId1: String, userId2: String): Chat {
        return createPrivateChat(userId1, userId2)
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

    override suspend fun findGroupChatByEventId(eventId: String): Chat? {
        println("ChatsRepository: Finding group chat for eventId=$eventId")
        val chatDto = supabaseWrapper.postgrest[Table.CHATS]
            .select { filter { eq("type", "group"); eq("event_id", eventId) } }
            .decodeSingleOrNull<ChatDto>()

        return chatDto?.let { dto ->
            val participantIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                .select { filter { eq("chat_id", dto.id) } }
                .decodeList<ChatParticipantDto>()
                .map { it.userId }
            val event = eventsRepository.fetchEventById(eventId)
            val lastMessage = supabaseWrapper.postgrest[Table.MESSAGES]
                .select { filter { eq("chat_id", dto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                .decodeSingleOrNull<MessageDto>()
            dto.toDomain(
                event = event,
                participantIds = participantIds,
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.createdAt,
                lastMessageSenderId = lastMessage?.senderId,
                unreadCount = supabaseWrapper.auth.currentUserOrNull()?.id?.let { getUnreadCount(dto.id, it) } ?: 0,
                isMessageRead = lastMessage?.isRead
            )
        }.also {
            println("ChatsRepository: Found group chat for eventId=$eventId: $it")
        }
    }

    override suspend fun removeUserFromChat(chatId: String, userId: String) {
        println("ChatsRepository: Removing userId=$userId from chatId=$chatId")
        try {
            // Remove user from chat participants
            supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                .delete { filter { eq("chat_id", chatId); eq("user_id", userId) } }

            // Check remaining participants
            val remainingParticipants = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                .select { filter { eq("chat_id", chatId) } }
                .decodeList<ChatParticipantDto>()

            if (remainingParticipants.isEmpty()) {
                // Delete the chat if no participants remain
                deleteChat(chatId)
                println("ChatsRepository: No participants left, deleted chatId=$chatId")
            }
        } catch (e: Exception) {
            println("ChatsRepository: Error removing user from chat: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun createGroupChat(eventId: String, userId: String): Chat {
        println("ChatsRepository: Creating group chat for eventId=$eventId, userId=$userId")
        try {
            // Проверяем, существует ли чат
            val existingChat = supabaseWrapper.postgrest[Table.CHATS]
                .select {
                    filter { eq("event_id", eventId) }
                    filter { eq("type", "group") }
                }
                .decodeSingleOrNull<ChatDto>()
            if (existingChat != null) {
                println("ChatsRepository: Found group chat for eventId=$eventId: $existingChat")
                // Проверяем, является ли пользователь участником
                val isParticipant = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                    .select {
                        filter { eq("chat_id", existingChat.id) }
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<ChatParticipantDto>()
                    .isNotEmpty()
                if (!isParticipant) {
                    // Добавляем пользователя в chat_participants
                    val participantDto = ChatParticipantDto(
                        chatId = existingChat.id,
                        userId = userId,
                        lastSeen = OffsetDateTime.now().toString(),
                        lastReadAt = OffsetDateTime.now().toString()
                    )
                    supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                        .insert(participantDto)
                    println("ChatsRepository: Added userId=$userId to chat_participants for chatId=${existingChat.id}")
                }
                val chat = fetchChatById(existingChat.id)
                    ?: throw IllegalStateException("Chat not found after adding participant")
                println("ChatsRepository: Returning existing group chat id=${chat.id} for eventId=$eventId")
                return chat
            }

            // Если чат не существует, создаём новый
            val event = eventsRepository.fetchEventById(eventId)
                ?: throw IllegalArgumentException("Event not found: $eventId")
            val participantIds = supabaseWrapper.postgrest[Table.EVENT_PARTICIPANTS]
                .select { filter { eq("event_id", eventId) } }
                .decodeList<EventParticipantDto>()
                .map { it.userId }
                .distinct()
            println("ChatsRepository: Found ${participantIds.size} participants for eventId=$eventId: $participantIds")
            if (participantIds.isEmpty()) {
                throw IllegalStateException("No participants found for eventId=$eventId")
            }
            val chatDto = ChatDto(
                id = UUID.randomUUID().toString(),
                type = "group",
                eventId = eventId,
                createdAt = OffsetDateTime.now().toString(),
                updatedAt = OffsetDateTime.now().toString()
            )
            supabaseWrapper.postgrest[Table.CHATS].insert(chatDto)
            println("ChatsRepository: Created new group chat: $chatDto")
            val participants = participantIds.map { participantId ->
                ChatParticipantDto(
                    chatId = chatDto.id,
                    userId = participantId.toString(),
                    lastSeen = OffsetDateTime.now().toString(),
                    lastReadAt = OffsetDateTime.now().toString()
                ).also {
                    println("ChatsRepository: Adding participant to chatId=${chatDto.id}: $it")
                }
            }
            // Убедимся, что текущий пользователь добавлен
            if (userId !in participantIds) {
                participants + ChatParticipantDto(
                    chatId = chatDto.id,
                    userId = userId,
                    lastSeen = OffsetDateTime.now().toString(),
                    lastReadAt = OffsetDateTime.now().toString()
                ).also {
                    println("ChatsRepository: Adding current userId=$userId to chatId=${chatDto.id}")
                }
            }
            supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS].insert(participants)
            val chat = fetchChatById(chatDto.id)
                ?: throw IllegalStateException("Failed to fetch created chat: ${chatDto.id}")
            println("ChatsRepository: Successfully created group chat: $chat")
            return chat
        } catch (e: Exception) {
            println("ChatsRepository: Error creating group chat for eventId=$eventId, userId=$userId: ${e.message}")
            throw e
        }
    }

    override suspend fun fetchMessages(chatId: String): List<Message> {
        println("MessagesRepository: Fetching messages for chatId=$chatId")
        val chat = fetchChatById(chatId) ?: throw IllegalArgumentException("Chat not found: $chatId")
        val isGroupChat = chat.type == "group"
        val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id ?: throw IllegalStateException("User not authenticated")
        val messages = supabaseWrapper.postgrest[Table.MESSAGES]
            .select {
                filter {
                    eq("chat_id", chatId)
                    raw("NOT deleted_by @> ARRAY['$currentUserId']::text[]") // Basic PostgreSQL filter
                }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<MessageDto>()
        return messages.map { messageDto ->
            val senderName = if (isGroupChat && messageDto.senderId != currentUserId) {
                usersRepository.fetchById(messageDto.senderId)?.name ?: "Unknown"
            } else {
                null
            }
            messageDto.toDomain(senderName)
        }.also {
            println("MessagesRepository: Fetched ${it.size} messages for chatId=$chatId")
        }
    }

    override suspend fun createPrivateChat(userId1: String, userId2: String): Chat {
        println("ChatsRepository: Creating private chat for userId1=$userId1, userId2=$userId2")
        val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated user found")

        if (currentUserId != userId1) {
            throw IllegalStateException("Current user $currentUserId does not match userId1 $userId1")
        }

        val chatIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
            .select { filter { eq("user_id", currentUserId) } }
            .decodeList<ChatParticipantDto>()
            .map { it.chatId }
        println("ChatsRepository: Found ${chatIds.size} chatIds for userId1=$currentUserId: $chatIds")

        var existingChat: ChatDto? = null
        for (chatId in chatIds) {
            val chat = supabaseWrapper.postgrest[Table.CHATS]
                .select { filter { eq("type", "private"); eq("id", chatId) } }
                .decodeSingleOrNull<ChatDto>()
            if (chat != null) {
                val participants = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                    .select { filter { eq("chat_id", chat.id) } }
                    .decodeList<ChatParticipantDto>()
                    .map { it.userId }
                println("ChatsRepository: Checking participants for chatId=${chat.id}: $participants")
                if (participants.containsAll(listOf(currentUserId, userId2))) {
                    existingChat = chat
                    break
                }
            }
        }

        if (existingChat != null) {
            val participantUser = usersRepository.fetchById(userId2)
            val participantIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                .select { filter { eq("chat_id", existingChat.id) } }
                .decodeList<ChatParticipantDto>()
                .map { it.userId }
            println("ChatsRepository: Found existing private chat id=${existingChat.id}, participantUser=$participantUser, participantIds=$participantIds")
            return existingChat.toDomain(
                participantName = participantUser?.name,
                participantUser = participantUser,
                participantIds = participantIds
            ).also {
                println("ChatsRepository: Returning existing private chat id=${it.id} for users $currentUserId, $userId2")
            }
        }

        val chatDto = ChatDto(
            id = UUID.randomUUID().toString(),
            type = "private",
            eventId = null,
            createdAt = OffsetDateTime.now().toString(),
            updatedAt = OffsetDateTime.now().toString()
        )
        println("ChatsRepository: Creating new chat: $chatDto")

        supabaseWrapper.postgrest[Table.CHATS].insert(chatDto)

        listOf(currentUserId, userId2).forEach { userId ->
            val participantDto = ChatParticipantDto(
                chatId = chatDto.id,
                userId = userId,
                lastReadAt = null
            )
            println("ChatsRepository: Adding participant to chatId=${chatDto.id}: $participantDto")
            try {
                supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS].insert(participantDto)
            } catch (e: Exception) {
                println("ChatsRepository: Failed to insert participant $participantDto, error=${e.message}")
                throw e
            }
        }

        val participantUser = usersRepository.fetchById(userId2)
        return chatDto.toDomain(
            participantName = participantUser?.name,
            participantUser = participantUser,
            participantIds = listOf(currentUserId, userId2)
        ).also {
            println("ChatsRepository: Created private chat id=${it.id} for users $currentUserId, $userId2")
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

    override suspend fun leaveGroupChat(chatId: String, userId: String) {
        println("ChatsRepository: Leaving group chat id=$chatId for userId=$userId")
        val chat = fetchChatById(chatId) ?: throw IllegalArgumentException("Chat not found: $chatId")
        if (chat.type != "group") {
            println("ChatsRepository: Cannot leave private chat id=$chatId")
            throw IllegalStateException("Выход возможен только из групповых чатов")
        }
        supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
            .delete {
                filter { eq("chat_id", chatId) }
                filter { eq("user_id", userId) }
            }
        println("ChatsRepository: User id=$userId removed from chat_participants for chatId=$chatId")
    }
    override suspend fun fetchUsersForChats(chatIds: List<String>): List<User> {
        return emptyList()
    }
}