package ru.walkAndTalk.data.repository

import io.github.jan.supabase.postgrest.query.Order
import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.model.ChatDto
import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.data.model.MessageDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.lang.System.`in`
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
            // Получаем chat_id из таблицы chat_participants
            val chatIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                .select { filter { eq("user_id", userId) } }
                .decodeList<ChatParticipantDto>()
                .map { it.chatId }
            println("ChatsRepository: Found ${chatIds.size} chatIds: $chatIds")
            if (chatIds.isEmpty()) return emptyList()

            // Получаем чаты по одному
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
                // Получаем participantIds из chat_participants
                val participantIds = supabaseWrapper.postgrest[Table.CHAT_PARTICIPANTS]
                    .select { filter { eq("chat_id", chatDto.id) } }
                    .decodeList<ChatParticipantDto>()
                    .map { it.userId }
                println("ChatsRepository: Fetched ${participantIds.size} participants for chatId=${chatDto.id}: $participantIds")

                val event = if (chatDto.type == "group" && chatDto.eventId != null) {
                    eventsRepository.fetchEventById(chatDto.eventId)
                } else null
                println("ChatsRepository: Event for chatId=${chatDto.id}: $event")

                val participantName = if (chatDto.type == "private") {
                    val otherUserId = participantIds.firstOrNull { it != userId }
                    otherUserId?.let { usersRepository.fetchById(it)?.name }
                } else null
                println("ChatsRepository: ParticipantName for chatId=${chatDto.id}: $participantName")

                val lastMessage = supabaseWrapper.postgrest[Table.MESSAGES]
                    .select { filter { eq("chat_id", chatDto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                    .decodeSingleOrNull<MessageDto>()
                println("ChatsRepository: Last message for chatId=${chatDto.id}: $lastMessage")

                chatDto.toDomain(
                    event = event,
                    participantName = participantName,
                    participantIds = participantIds,
                    lastMessage = lastMessage?.content,
                    lastMessageTime = lastMessage?.createdAt,
                    unreadCount = getUnreadCount(chatDto.id, userId)
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

            val event = if (dto.type == "group" && dto.eventId != null) {
                eventsRepository.fetchEventById(dto.eventId)
            } else null

            val currentUserId = supabaseWrapper.auth.currentUserOrNull()?.id
            val participantName = if (dto.type == "private" && currentUserId != null) {
                val otherUserId = participantIds.firstOrNull { it != currentUserId }
                otherUserId?.let { usersRepository.fetchById(it)?.name }
            } else null

            val lastMessage = supabaseWrapper.postgrest.from(Table.MESSAGES)
                .select { filter { eq("chat_id", dto.id) }; order("created_at", Order.DESCENDING); limit(1) }
                .decodeSingleOrNull<MessageDto>()

            dto.toDomain(
                event = event,
                participantName = participantName,
                participantIds = participantIds,
                lastMessage = lastMessage?.content,
                lastMessageTime = lastMessage?.createdAt,
                unreadCount = currentUserId?.let { getUnreadCount(dto.id, it) } ?: 0
            )
        }.also {
            println("ChatsRepository: Fetched chat id=$id, result=$it")
        }
    }

    override suspend fun createGroupChat(eventId: String, userId: String): Chat {
        // Проверяем, существует ли мероприятие
        val event = eventsRepository.fetchEventById(eventId)
            ?: throw IllegalArgumentException("Event not found: $eventId")

        // Получаем участников мероприятия
        val participantIds = supabaseWrapper.postgrest.from(Table.EVENT_PARTICIPANTS)
            .select { filter { eq("event_id", eventId) } }
            .decodeList<ChatParticipantDto>() // Предполагаем, что структура схожа
            .map { it.userId }

        // Создаём чат
        val chatDto = ChatDto(
            id = UUID.randomUUID().toString(),
            type = "group",
            eventId = eventId,
            createdAt = OffsetDateTime.now().toString(),
            updatedAt = OffsetDateTime.now().toString()
        )

        supabaseWrapper.postgrest.from(Table.CHATS).insert(chatDto)

        // Добавляем участников в chat_participants
        participantIds.forEach { participantId ->
            supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS).insert(
                ChatParticipantDto(
                    chatId = chatDto.id,
                    userId = participantId,
                    lastReadAt = null
                )
            )
        }

        return chatDto.toDomain(event = event, participantIds = participantIds).also {
            println("ChatsRepository: Created group chat id=${it.id} for eventId=$eventId")
        }
    }

    override suspend fun createPrivateChat(userId1: String, userId2: String): Chat {
        println("ChatsRepository: Creating private chat for userId1=$userId1, userId2=$userId2")
        // Проверяем, существует ли уже чат между пользователями
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
            val participantName = usersRepository.fetchById(userId2)?.name
            val participantIds = supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS)
                .select { filter { eq("chat_id", existingChat.id) } }
                .decodeList<ChatParticipantDto>()
                .map { it.userId }
            println("ChatsRepository: Found existing private chat id=${existingChat.id}, participantName=$participantName, participantIds=$participantIds")
            return existingChat.toDomain(participantName = participantName, participantIds = participantIds).also {
                println("ChatsRepository: Returning existing private chat id=${it.id} for users $userId1, $userId2")
            }
        }

        // Создаём новый чат
        val chatDto = ChatDto(
            id = UUID.randomUUID().toString(),
            type = "private",
            eventId = null,
            createdAt = OffsetDateTime.now().toString(),
            updatedAt = OffsetDateTime.now().toString()
        )
        println("ChatsRepository: Creating new chat: $chatDto")

        supabaseWrapper.postgrest.from(Table.CHATS).insert(chatDto)

        // Добавляем участников
        listOf(userId1, userId2).forEach { userId ->
            val participantDto = ChatParticipantDto(
                chatId = chatDto.id,
                userId = userId,
                lastReadAt = null
            )
            println("ChatsRepository: Adding participant to chatId=${chatDto.id}: $participantDto")
            supabaseWrapper.postgrest.from(Table.CHAT_PARTICIPANTS).insert(participantDto)
        }

        val participantName = usersRepository.fetchById(userId2)?.name
        return chatDto.toDomain(participantName = participantName, participantIds = listOf(userId1, userId2)).also {
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
}