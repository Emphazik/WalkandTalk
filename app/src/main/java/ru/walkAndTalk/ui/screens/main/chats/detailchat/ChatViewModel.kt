package ru.walkAndTalk.ui.screens.main.chats.detailchat

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.MessagesRepository
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.PostgresChangeFilter
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.RealtimeChannelBuilder
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull.content
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.domain.model.NotificationType
import ru.walkAndTalk.domain.repository.NotificationsRepository
import ru.walkAndTalk.domain.repository.RemoteUsersRepository
import java.time.OffsetDateTime
import java.util.UUID

class ChatViewModel(
    private val chatId: String,
    private val userId: String,
    private val chatsRepository: ChatsRepository,
    private val usersRepository: RemoteUsersRepository,
    private val messagesRepository: MessagesRepository,
    private val notificationsRepository: NotificationsRepository,
    private val supabaseWrapper: SupabaseWrapper
) : ContainerHost<ChatViewState, ChatSideEffect>, ViewModel() {

    override val container = container<ChatViewState, ChatSideEffect>(
        ChatViewState(chatId = chatId, currentUserId = userId)
    ) {
        loadChat()
        updateParticipantLastSeen()
        subscribeToMessages()
        subscribeToParticipantActivity()
        subscribeToMessageReadUpdates()
        startPollingMessages()
    }

    private fun subscribeToMessages() = intent {
        if (isSubscribed) {
            println("ChatViewModel: Already subscribed to messages for chatId=$chatId")
            return@intent
        }
        try {
            messagesChannel = supabaseWrapper.realtime.channel("messages-channel-$chatId")
            println("ChatViewModel: Subscribing to channel: messages-channel-$chatId")
            val chat = chatsRepository.fetchChatById(chatId) ?: throw IllegalStateException("Chat not found: $chatId")
            val isGroupChat = chat.type == "group"

            val insertFlow = messagesChannel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "messages"
                filter("chat_id", FilterOperator.EQ, chatId)
            }
            val updateFlow = messagesChannel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "messages"
                filter("chat_id", FilterOperator.EQ, chatId)
            }
            val deleteFlow = messagesChannel.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                table = "messages"
                filter("chat_id", FilterOperator.EQ, chatId)
            }

            viewModelScope.launch {
                insertFlow.collect { payload ->
                    println("ChatViewModel: Received insert payload: $payload")
                    val record = payload.record as Map<String, JsonElement>
                    val messageId = record["id"]?.jsonPrimitive?.content ?: ""
                    val messageChatId = record["chat_id"]?.jsonPrimitive?.content ?: ""
                    val senderId = record["sender_id"]?.jsonPrimitive?.content ?: ""
                    val content = record["content"]?.jsonPrimitive?.content ?: ""
                    val createdAt = record["created_at"]?.jsonPrimitive?.content ?: ""
                    val isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false
                    val deletedBy = record["deleted_by"]?.let { element ->
                        if (element is JsonArray) element.mapNotNull { it.jsonPrimitive.contentOrNull } else emptyList()
                    } ?: emptyList()

                    if (messageId.isEmpty() || content.isEmpty() || senderId.isEmpty()) {
                        println("ChatViewModel: Received invalid message from Realtime, id=$messageId, senderId=$senderId")
                        return@collect
                    }

                    if (state.messages.any { it.id == messageId }) {
                        println("ChatViewModel: Ignoring duplicate message from Realtime, id=$messageId")
                        return@collect
                    }

                    val senderName = if (isGroupChat && senderId != userId) {
                        usersRepository.fetchById(senderId)?.name ?: "Unknown"
                    } else {
                        null
                    }

                    val message = Message(
                        id = messageId,
                        chatId = messageChatId,
                        senderId = senderId,
                        content = content,
                        createdAt = createdAt,
                        isRead = isRead,
                        tempId = null,
                        deletedBy = deletedBy,
                        senderName = senderName
                    )

                    reduce {
                        val updatedMessages = (state.messages + message).sortedBy {
                            OffsetDateTime.parse(it.createdAt).toInstant().toEpochMilli()
                        }.toList()
                        state.copy(messages = updatedMessages)
                    }
                    println("ChatViewModel: Received new message for chatId=$chatId, message=$message")
                    if (senderId != userId) {
                        markMessageAsRead(message)
                    }
                }
            }
            viewModelScope.launch {
                updateFlow.collect { payload ->
                    println("ChatViewModel: Received update payload: $payload")
                    val record = payload.record as Map<String, JsonElement>
                    val messageId = record["id"]?.jsonPrimitive?.content ?: ""
                    val content = record["content"]?.jsonPrimitive?.content ?: ""
                    val isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false
                    val deletedBy = record["deleted_by"]?.let { element ->
                        if (element is JsonArray) element.mapNotNull { it.jsonPrimitive.contentOrNull } else emptyList()
                    } ?: emptyList()
                    if (messageId.isEmpty()) {
                        println("ChatViewModel: Received invalid update from Realtime, id=$messageId")
                        return@collect
                    }
                    val updatedMessages = state.messages.mapNotNull { msg ->
                        if (msg.id == messageId) {
                            if (userId in deletedBy) {
                                println("ChatViewModel: Message id=$messageId deleted for userId=$userId")
                                null
                            } else {
                                println("ChatViewModel: Updating message id=$messageId with content=$content, isRead=$isRead, deletedBy=$deletedBy")
                                msg.copy(content = content, isRead = isRead, deletedBy = deletedBy)
                            }
                        } else {
                            msg
                        }
                    }.toList() // Ensure new list
                    reduce { state.copy(messages = updatedMessages.sortedBy { OffsetDateTime.parse(it.createdAt).toInstant().toEpochMilli() }) }
                    println("ChatViewModel: Updated messages state: $updatedMessages, last message isRead=${updatedMessages.lastOrNull()?.isRead}")
                }
            }
            viewModelScope.launch {
                deleteFlow.collect { payload ->
                    println("ChatViewModel: Received delete payload: $payload")
                    val record = payload.oldRecord as Map<String, JsonElement>
                    val messageId = record["id"]?.jsonPrimitive?.content ?: ""
                    if (messageId.isEmpty()) {
                        println("ChatViewModel: Invalid delete from Realtime, id=$messageId")
                        return@collect
                    }
                    val updatedMessages = state.messages
                        .filter { it.id != messageId }
                        .sortedBy { OffsetDateTime.parse(it.createdAt).toInstant().toEpochMilli() }
                        .toList()
                    reduce { state.copy(messages = updatedMessages) }
                    println("ChatViewModel: Deleted message id=$messageId from state, messagesCount=${updatedMessages.size}, lastMessageId=${updatedMessages.lastOrNull()?.id}")
                }
            }

            messagesChannel.subscribe()
            isSubscribed = true
            println("ChatViewModel: Successfully subscribed to messages for chatId=$chatId")
        } catch (e: Exception) {
            println("ChatViewModel: Error subscribing to messages: ${e.message}")
//            postSideEffect(ChatSideEffect.ShowError("Не удалось подключиться к обновлениям сообщений"))
        }
    }

    private fun subscribeToParticipantActivity() = intent {
        try {
            participantsChannel = supabaseWrapper.realtime.channel("participants-channel-$chatId")
            val changeFlow = participantsChannel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "chat_participants"
                filter("chat_id", FilterOperator.EQ, chatId)
                filter("user_id", FilterOperator.NEQ, userId)
            }

            viewModelScope.launch {
                changeFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val lastSeenStr = record["last_seen"]?.jsonPrimitive?.content
                    val isActive = lastSeenStr?.let {
                        try {
                            val lastSeen = OffsetDateTime.parse(it)
                            lastSeen.isAfter(OffsetDateTime.now().minusMinutes(1))
                        } catch (e: Exception) {
                            false
                        }
                    } ?: false

                    reduce { state.copy(isParticipantActive = isActive) }
                    println("ChatViewModel: Participant active status updated: $isActive")
                }
            }

            println("ChatViewModel: Attempting to subscribe to participants channel for chatId=$chatId")
            participantsChannel.subscribe()
            println("ChatViewModel: Successfully subscribed to participant activity for chatId=$chatId")
        } catch (e: Exception) {
            println("ChatViewModel: Error subscribing to participant activity: ${e.message}")
//            postSideEffect(ChatSideEffect.ShowError("Не удалось обновить статус участников"))
        }
    }

fun onSendMessageClick(chatId: String) = intent {
    try {
        val trimmedContent = state.inputText.trim()
        if (trimmedContent.isEmpty()) {
            postSideEffect(ChatSideEffect.ShowError("Сообщение не может быть пустым"))
            println("ChatViewModel: Attempted to send empty message")
            return@intent
        }

        val tempId = UUID.randomUUID().toString()
        val tempMessage = Message(
            id = tempId,
            chatId = chatId,
            senderId = userId,
            content = trimmedContent,
            createdAt = OffsetDateTime.now().toString(),
            isRead = false,
            tempId = tempId
        )

        reduce { state.copy(messages = state.messages + tempMessage, inputText = "") }
        println("ChatViewModel: Locally added temp message: $tempMessage")

        val serverMessage = messagesRepository.sendMessage(chatId, userId, trimmedContent)
        println("ChatViewModel: Sent message to chatId=$chatId, server response: $serverMessage")

        // Fetch chat to determine type and participants
        val chat = chatsRepository.fetchChatById(chatId) ?: throw IllegalStateException("Chat not found")
        val participants = chatsRepository.getChatParticipants(chatId)
        val recipientIds = participants
            .filter { it.userId != userId && it.userId.isNotBlank() }
            .map { it.userId }

        // Create notifications for recipients
        if (recipientIds.isNotEmpty()) {
            val senderName = usersRepository.fetchById(userId)?.name ?: "Пользователь"
            val notificationContent = if (chat.type == "group") {
                "Новое сообщение в чате \"${chat.eventName ?: "группе"}\" от $senderName: $trimmedContent"
            } else {
                "Новое сообщение от $senderName: $trimmedContent"
            }
            try {
                notificationsRepository.createNotifications(
                    userIds = recipientIds,
                    type = NotificationType.NewMessage.name,
                    content = notificationContent,
                    relatedId = chatId
                )
                println("ChatViewModel: Created notifications for ${recipientIds.size} recipients")
            } catch (e: Exception) {
                println("ChatViewModel: Error creating notifications: ${e.message}")
                postSideEffect(ChatSideEffect.ShowError("Не удалось отправить уведомления: ${e.message}"))
            }
        }

        // Удаляем локальное сообщение и добавляем серверное
        val updatedMessages = (state.messages.filter { it.id != tempId && it.id != serverMessage.id } + serverMessage)
            .sortedBy { OffsetDateTime.parse(it.createdAt).toInstant().toEpochMilli() }
            .toList()
        reduce { state.copy(messages = updatedMessages) }
        println("ChatViewModel: Replaced temp message with server message: ${serverMessage.id}, isRead=${serverMessage.isRead}")
    } catch (e: Exception) {
        reduce { state.copy(messages = state.messages.dropLast(1), inputText = state.inputText) }
        postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка отправки сообщения"))
        println("ChatViewModel: Error sending message: ${e.message}")
    }
}
    fun forceUpdateReadStatus() = intent {
        val updatedMessages = state.messages.map { msg ->
            if (msg.isRead) {
                msg
            } else {
                val isReadInDb = try {
                    messagesRepository.getMessageById(msg.id)?.isRead ?: false
                } catch (e: Exception) {
                    println("ChatViewModel: Error fetching isRead for message ${msg.id}: ${e.message}")
                    false
                }
                msg.copy(isRead = isReadInDb)
            }
        }.sortedBy { OffsetDateTime.parse(it.createdAt).toInstant().toEpochMilli() }.toList()
        reduce { state.copy(messages = updatedMessages) }
        println("ChatViewModel: Forced read status update, messagesCount=${updatedMessages.size}, lastMessageId=${updatedMessages.lastOrNull()?.id}, isRead=${updatedMessages.lastOrNull()?.isRead}")
    }

    fun forceUpdateMessages() = intent {
        delay(500) // Ждём 500 мс для Realtime
        try {
            val messagesFromDb = messagesRepository.getMessagesByChatId(chatId)
            val updatedMessages = messagesFromDb
                .filter { userId !in (it.deletedBy ?: emptyList()) }
                .sortedBy { OffsetDateTime.parse(it.createdAt).toInstant().toEpochMilli() }
                .toList()
            reduce { state.copy(messages = updatedMessages) }
            println("ChatViewModel: Forced messages update, messagesCount=${updatedMessages.size}, lastMessageId=${updatedMessages.lastOrNull()?.id}")
        } catch (e: Exception) {
            println("ChatViewModel: Error forcing messages update: ${e.message}")
            postSideEffect(ChatSideEffect.ShowError("Не удалось обновить сообщения: ${e.message}"))
        }
    }

    private fun subscribeToMessageReadUpdates() = intent {
        println("ChatViewModel: Message read updates handled in subscribeToMessages")
    }

    private lateinit var messagesChannel: RealtimeChannel
    private lateinit var participantsChannel: RealtimeChannel
    private var pollingJob: Job? = null
    private var isSubscribed = false // Флаг для предотвращения повторной подписки

    private fun startPollingMessages() = intent {
        println("ChatViewModel: Polling disabled for chatId=$chatId, using Realtime subscriptions")
    }

    fun onInputTextChange(value: String) = intent {
        reduce { state.copy(inputText = value) }
    }

    fun clearSelection() = intent {
        reduce { state.copy(selectedMessageIds = emptySet()) }
    }

    // Poisk
    fun toggleSearch() = intent {
        reduce { state.copy(isSearchActive = !state.isSearchActive, searchQuery = if (state.isSearchActive) "" else state.searchQuery) }
    }

    fun updateSearchQuery(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
    }
    //
    fun showCopySuccess(message: String? = null) = intent {
        val sideEffect = message?.let {
            ChatSideEffect.ShowCopySuccess(it)
        } ?: ChatSideEffect.ShowCopySuccess()
        postSideEffect(sideEffect)
    }

    fun loadChat() = intent {
        try {
            val chat = chatsRepository.fetchChatById(chatId)
            val messages = messagesRepository.fetchMessages(chatId)
            reduce { state.copy(chat = chat, messages = messages, isLoading = false) }
            println("ChatViewModel: Loaded chat=$chat, messages=${messages.size}")
            markAllMessagesAsRead(messages)
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
                //postSideEffect(ChatSideEffect.ShowError("Не удалось загрузить чат. Проверьте соединение или попробуйте позже."))
            println("ChatViewModel: Error loading chat: ${e.message}")
        }
    }

    fun getParticipantCount(): Int {
        return runBlocking {
            supabaseWrapper.postgrest.from("chat_participants")
                .select { filter { eq("chat_id", chatId) } }
                .decodeList<ChatParticipantDto>()
                .size
        }
    }

    fun onEditClick(message: Message) = intent {
        reduce {
            state.copy(
                editingMessageId = message.id,
                inputText = message.content,
                showEditDialog = true,
            )
        }
    }

    fun editMessage(messageId: String, value: String) = intent {
        val messageToEdit = state.messages.find { it.id == messageId && it.senderId == userId }
        if (messageToEdit != null) {
            try {
                val updatedMessage = messagesRepository.editMessage(messageId, value)
                val updatedMessages = state.messages.map { if (it.id == messageId) updatedMessage else it }
                reduce {
                    state.copy(
                        messages = updatedMessages,
                        selectedMessageIds = emptySet(),
                        editingMessageId = null,
                        inputText = "", // Очищаем текстовое поле
                        showEditDialog = false // Закрываем диалог
                    )
                }

                println("ChatViewModel: Edited message id=$messageId, newContent=$value, inputText cleared")
            } catch (e: Exception) {
                postSideEffect(ChatSideEffect.ShowError("Не удалось отредактировать сообщение: ${e.message}"))
                println("ChatViewModel: Error editing message: ${e.message}")
            }
        } else {
            postSideEffect(ChatSideEffect.ShowError("Вы не можете редактировать это сообщение"))
            println("ChatViewModel: No permission to edit message: $messageId")
        }
    }

    fun onDeleteMessagesClick(messageIds: List<String>, isLocal: Boolean = false) = intent {
        val messagesToDelete = state.messages.filter { it.id in messageIds && it.senderId == userId }
        if (messagesToDelete.isNotEmpty()) {
            if (isLocal) {
                val updatedMessages = state.messages.filterNot { it.id in messageIds }
                reduce { state.copy(messages = updatedMessages, selectedMessageIds = emptySet()) }
                println("ChatViewModel: Locally deleted messages: $messageIds")
            } else {
                try {
                    messagesRepository.deleteMessages(messagesToDelete.map { it.id }) // Only delete own messages
                    val updatedMessages = state.messages.filterNot { it.id in messagesToDelete.map { it.id } }
                    reduce { state.copy(messages = updatedMessages, selectedMessageIds = emptySet()) }
                    println("ChatViewModel: Deleted messages on server: ${messagesToDelete.map { it.id }}")
                } catch (e: Exception) {
                    postSideEffect(ChatSideEffect.ShowError("Не удалось удалить сообщения: ${e.message}"))
                    println("ChatViewModel: Error deleting messages: ${e.message}")
                }
            }
        } else {
            postSideEffect(ChatSideEffect.ShowError("Вы не можете удалить сообщения собеседника"))
            println("ChatViewModel: No permission to delete messages: $messageIds")
        }
        toggleShowDeleteDialog() // Close dialog
    }

    fun toggleShowDeleteDialog() = intent {
        reduce { state.copy(showDeleteDialog = !state.showDeleteDialog) }
    }

    fun toggleShowEditDialog() = intent {
        reduce { state.copy(showEditDialog = !state.showEditDialog) }
    }

    fun toggleMessageSelection(messageId: String) = intent {
        val currentSelection = state.selectedMessageIds
        val newSelection = if (messageId in currentSelection) {
            currentSelection - messageId
        } else {
            currentSelection + messageId
        }
        reduce { state.copy(selectedMessageIds = newSelection) }
        println("ChatViewModel: Toggled selection, selectedMessageIds=$newSelection")
    }



//    private fun subscribeToParticipantActivity() = intent {
//        try {
//            participantsChannel = supabaseWrapper.realtime.channel("participants-channel-$chatId")
//
//            val changeFlow = participantsChannel.postgresChangeFlow<PostgresAction.Update>(
//                schema = "public"
//            ) {
//                filter("chat_id", FilterOperator.EQ, chatId)
//                filter("user_id", FilterOperator.NEQ, userId)
//            }
//
//            viewModelScope.launch {
//                changeFlow.collect { payload ->
//                    val record = payload.record as Map<String, JsonElement>
//                    val lastSeenStr = record["last_seen"]?.jsonPrimitive?.content
//                    val isActive = lastSeenStr?.let {
//                        try {
//                            val lastSeen = OffsetDateTime.parse(it)
//                            lastSeen.isAfter(OffsetDateTime.now().minusMinutes(1))
//                        } catch (e: Exception) {
//                            false
//                        }
//                    } ?: false
//
//                    reduce { state.copy(isParticipantActive = isActive) }
//                    println("ChatViewModel: Participant active status updated: $isActive")
//                }
//            }
//
//            println("ChatViewModel: Attempting to subscribe to participants channel for chatId=$chatId")
//            participantsChannel.subscribe()
//            println("ChatViewModel: Successfully subscribed to participant activity for chatId=$chatId")
//        } catch (e: Exception) {
//            println("ChatViewModel: Error subscribing to participant activity: ${e.message}")
//            //postSideEffect(ChatSideEffect.ShowError("Не удалось обновить статус участников. Попробуйте перезайти в чат позже."))        }
//        }
//    }

    override fun onCleared() {
        viewModelScope.launch {
            pollingJob?.cancel()
            println("ChatViewModel: Stopped polling messages for chatId=$chatId")
            if (::messagesChannel.isInitialized) {
                messagesChannel.unsubscribe()
                supabaseWrapper.realtime.removeChannel(messagesChannel)
            }
            if (::participantsChannel.isInitialized) {
                participantsChannel.unsubscribe()
                supabaseWrapper.realtime.removeChannel(participantsChannel)
            }
            isSubscribed = false // Сбрасываем флаг
            println("ChatViewModel: Unsubscribed from channels for chatId=$chatId")
        }
        super.onCleared()
    }

    private fun updateParticipantLastSeen() = intent {
        try {
            supabaseWrapper.postgrest.from("chat_participants")
                .update(
                    mapOf("last_seen" to OffsetDateTime.now().toString())
                ) {
                    filter { eq("chat_id", chatId) }
                    filter { eq("user_id", userId) }
                }
            println("ChatViewModel: Updated lastSeen for userId=$userId in chatId=$chatId")
        } catch (e: Exception) {
            println("ChatViewModel: Error updating lastSeen: ${e.message}")
        }
    }

    private fun markAllMessagesAsRead(messages: List<Message>) = intent {
        try {
            val unreadMessages = messages.filter { !it.isRead && it.senderId != userId }
            unreadMessages.forEach { message ->
                messagesRepository.markMessageAsRead(message.id)
                println("ChatViewModel: Marked message as read: ${message.id}")
            }
            val updatedMessages = messages.map { if (it in unreadMessages) it.copy(isRead = true) else it }
            reduce { state.copy(messages = updatedMessages) }
            chatsRepository.markChatAsRead(chatId, userId)
            val updatedChat = state.chat?.copy(unreadCount = 0)
            reduce { state.copy(chat = updatedChat) }
            // Обновляем lastReadAt
            supabaseWrapper.postgrest.from("chat_participants")
                .update(mapOf("last_read_at" to OffsetDateTime.now().toString())) {
                    filter { eq("chat_id", chatId) }
                    filter { eq("user_id", userId) }
                }
        } catch (e: Exception) {
            println("ChatViewModel: Error marking messages as read: ${e.message}")
            postSideEffect(ChatSideEffect.ShowError("Ошибка отметки сообщений как прочитанных: ${e.message}"))
        }
    }

    private fun markMessageAsRead(message: Message) = intent {
        try {
            messagesRepository.markMessageAsRead(message.id)
            val updatedMessages = state.messages.map {
                if (it.id == message.id) it.copy(isRead = true) else it
            }
            reduce { state.copy(messages = updatedMessages) }
            println("ChatViewModel: Marked message as read: ${message.id}, state.messages=${state.messages}")
        } catch (e: Exception) {
            println("ChatViewModel: Error marking message as read: ${e.message}, stacktrace: ${e.stackTraceToString()}")
            postSideEffect(ChatSideEffect.ShowError("Ошибка отметки сообщения как прочитанного: ${e.message}"))
        }
    }

    fun toggleMuteChat(chatId: String, mute: Boolean) = intent {
        try {
            chatsRepository.toggleMuteChat(chatId, mute)
            val updatedChat = state.chat?.copy(isMuted = mute)
            reduce { state.copy(chat = updatedChat) }
            println("ChatViewModel: Toggled mute for chatId=$chatId, mute=$mute")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка изменения уведомлений"))
            println("ChatViewModel: Error toggling mute: ${e.message}")
        }
    }

    fun markChatAsRead(chatId: String) = intent {
        try {
            chatsRepository.markChatAsRead(chatId, userId)
            val updatedChat = state.chat?.copy(unreadCount = 0)
            reduce { state.copy(chat = updatedChat) }
            println("ChatViewModel: Marked chatId=$chatId as read")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка отметки прочитанным"))
            println("ChatViewModel: Error marking as read: ${e.message}")
        }
    }

    fun clearChatHistory(chatId: String) = intent {
        try {
            chatsRepository.clearChatHistory(chatId, userId)
            val updatedChat = state.chat?.copy(lastMessage = null, lastMessageTime = null, unreadCount = 0)
            reduce { state.copy(chat = updatedChat, messages = emptyList()) }
            println("ChatViewModel: Cleared history for chatId=$chatId for userId=$userId")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError("Не удалось очистить историю чата. Пожалуйста, попробуйте еще раз или обратитесь в поддержку."))
            println("ChatViewModel: Error clearing history: ${e.message}")
        }
    }
}