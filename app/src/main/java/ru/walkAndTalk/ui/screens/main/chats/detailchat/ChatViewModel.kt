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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull.content
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import java.time.OffsetDateTime
import java.util.UUID

class ChatViewModel(
    private val chatId: String,
    private val userId: String,
    private val chatsRepository: ChatsRepository,
    private val messagesRepository: MessagesRepository,
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
    }

    private lateinit var messagesChannel: RealtimeChannel
    private lateinit var participantsChannel: RealtimeChannel

    fun onInputTextChange(value: String) = intent {
        reduce { state.copy(inputText = value) }
    }

    fun clearSelection() = intent {
        reduce { state.copy(selectedMessageIds = emptySet()) }
    }

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
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка загрузки чата"))
            println("ChatViewModel: Error loading chat: ${e.message}")
        }
    }

    fun onDeleteMessagesClick(
        messageIds: List<String>,
        isLocal: Boolean = false
    ) = intent {
        val messagesToDelete = state.messages.filter { it.id in messageIds && it.senderId == userId }
        if (messagesToDelete.isNotEmpty()) {
            if (isLocal) {
                val updatedMessages = state.messages.filterNot { it.id in messageIds }
                reduce { state.copy(messages = updatedMessages, selectedMessageIds = emptySet()) }
                println("ChatViewModel: Locally deleted messages: $messageIds")
            } else {
                try {
                    messagesRepository.deleteMessages(messageIds)
                    val updatedMessages = state.messages.filterNot { it.id in messageIds }
                    reduce { state.copy(messages = updatedMessages, selectedMessageIds = emptySet()) }
                    println("ChatViewModel: Deleted messages on server: $messageIds")
                } catch (e: Exception) {
                    postSideEffect(ChatSideEffect.ShowError("Не удалось удалить сообщения: ${e.message}"))
                    println("ChatViewModel: Error deleting messages: ${e.message}")
                }
            }
            toggleShowDeleteDialog()
        } else {
            postSideEffect(ChatSideEffect.ShowError("Вы не можете удалить сообщения собеседника"))
            println("ChatViewModel: No permission to delete messages: $messageIds")
        }
    }

    private fun deleteMessages(messageIds: List<String>) = intent {
        try {
            messagesRepository.deleteMessages(messageIds)
            val updatedMessages = state.messages.filter { it.id !in messageIds }
            reduce { state.copy(messages = updatedMessages, selectedMessageIds = emptySet()) }
            println("ChatViewModel: Deleted messages: $messageIds")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка удаления сообщений"))
            println("ChatViewModel: Error deleting messages: ${e.message}")
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
                reduce { state.copy(messages = updatedMessages, selectedMessageIds = emptySet(), editingMessageId = null) }
                println("ChatViewModel: Edited message id=$messageId, newContent=$value")
                toggleShowEditDialog()
            } catch (e: Exception) {
                postSideEffect(ChatSideEffect.ShowError("Не удалось отредактировать сообщение: ${e.message}"))
                println("ChatViewModel: Error editing message: ${e.message}")
            }
        } else {
            postSideEffect(ChatSideEffect.ShowError("Вы не можете редактировать это сообщение"))
            println("ChatViewModel: No permission to edit message: $messageId")
        }
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

            // Удаляем локальное сообщение и добавляем серверное
            val updatedMessages = state.messages.filter { it.tempId != tempId } + serverMessage
            reduce { state.copy(messages = updatedMessages) }
            println("ChatViewModel: Replaced temp message with server message: ${serverMessage.id}")
        } catch (e: Exception) {
            reduce { state.copy(messages = state.messages.dropLast(1), inputText = state.inputText) }
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка отправки сообщения"))
            println("ChatViewModel: Error sending message: ${e.message}")
        }
    }

    private fun subscribeToMessages() = intent {
        try {
            messagesChannel = supabaseWrapper.realtime.channel("messages-channel-$chatId")

            val insertFlow = messagesChannel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                filter("chat_id", FilterOperator.EQ, chatId)
            }

            val updateFlow = messagesChannel.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                filter("chat_id", FilterOperator.EQ, chatId)
            }

            viewModelScope.launch {
                insertFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val messageId = record["id"]?.jsonPrimitive?.content ?: ""
                    val messageChatId = record["chat_id"]?.jsonPrimitive?.content ?: ""
                    val senderId = record["sender_id"]?.jsonPrimitive?.content ?: ""
                    val content = record["content"]?.jsonPrimitive?.content ?: ""
                    val createdAt = record["created_at"]?.jsonPrimitive?.content ?: ""
                    val isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false

                    if (messageId.isEmpty() || content.isEmpty()) {
                        println("ChatViewModel: Received invalid message from Realtime, id=$messageId")
                        return@collect
                    }

                    val message = Message(
                        id = messageId,
                        chatId = messageChatId,
                        senderId = senderId,
                        content = content,
                        createdAt = createdAt,
                        isRead = isRead
                    )

                    // Проверяем, не является ли это сообщение уже добавленным через локальную отправку
                    if (state.messages.any { it.id == messageId || (it.tempId != null && it.senderId == senderId && it.content == content) }) {
                        println("ChatViewModel: Ignoring duplicate message from Realtime, id=$messageId")
                        return@collect
                    }

                    reduce { state.copy(messages = state.messages + message) }
                    println("ChatViewModel: Received new message for chatId=$chatId, message=$message")
                    if (message.senderId != userId) {
                        markMessageAsRead(message)
                    }
                }

                updateFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val messageId = record["id"]?.jsonPrimitive?.content ?: ""
                    val content = record["content"]?.jsonPrimitive?.content ?: ""
                    val isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false

                    if (messageId.isEmpty()) {
                        println("ChatViewModel: Received invalid update from Realtime, id=$messageId")
                        return@collect
                    }

                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(content = content, isRead = isRead)
                        } else {
                            msg
                        }
                    }
                    reduce { state.copy(messages = updatedMessages) }
                    println("ChatViewModel: Updated message for id=$messageId, new content=$content, isRead=$isRead")
                }
            }

            println("ChatViewModel: Attempting to subscribe to messages channel for chatId=$chatId")
            messagesChannel.subscribe()
            println("ChatViewModel: Successfully subscribed to messages for chatId=$chatId")
        } catch (e: Exception) {
            println("ChatViewModel: Error subscribing to messages: ${e.message}")
        }
    }

    private fun subscribeToMessageReadUpdates() = intent {
        try {
            val updateFlow = messagesChannel.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                filter("chat_id", FilterOperator.EQ, chatId)
            }

            viewModelScope.launch {
                updateFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val messageId = record["id"]?.jsonPrimitive?.content ?: return@collect
                    val isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false

                    val updatedMessages = state.messages.map {
                        if (it.id == messageId) it.copy(isRead = isRead) else it
                    }
                    reduce { state.copy(messages = updatedMessages) }
                    println("ChatViewModel: Updated isRead for message id=$messageId, isRead=$isRead")
                }
            }
        } catch (e: Exception) {
            println("ChatViewModel: Error subscribing to message read updates: ${e.message}")
        }
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

    private fun subscribeToParticipantActivity() = intent {
        try {
            participantsChannel = supabaseWrapper.realtime.channel("participants-channel-$chatId")

            val changeFlow = participantsChannel.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                filter("chat_id", FilterOperator.EQ, chatId)
                filter("user_id", FilterOperator.NEQ, userId) // Исправили NOT_EQ на NEQ
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
            // Обновляем lastReadAt для текущего пользователя
            supabaseWrapper.postgrest.from("chat_participants")
                .update(
                    mapOf("last_read_at" to OffsetDateTime.now().toString())
                ) {
                    filter { eq("chat_id", chatId) }
                    filter { eq("user_id", userId) }
                }
        } catch (e: Exception) {
            println("ChatViewModel: Error marking messages as read: ${e.message}")
        }
    }

    private fun markMessageAsRead(message: Message) = intent {
        try {
            messagesRepository.markMessageAsRead(message.id)
            val updatedMessages = state.messages.map {
                if (it.id == message.id) it.copy(isRead = true) else it
            }
            reduce { state.copy(messages = updatedMessages) }
            println("ChatViewModel: Marked new message as read: ${message.id}")
        } catch (e: Exception) {
            println("ChatViewModel: Error marking message as read: ${e.message}")
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

    fun deleteChat(chatId: String) = intent {
        try {
            chatsRepository.deleteChat(chatId)
            println("ChatViewModel: Deleted chatId=$chatId")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка удаления чата"))
            println("ChatViewModel: Error deleting chat: ${e.message}")
        }
    }

    fun clearChatHistory(chatId: String) = intent {
        try {
            chatsRepository.clearChatHistory(chatId)
            val updatedChat = state.chat?.copy(lastMessage = null, lastMessageTime = null, unreadCount = 0)
            reduce { state.copy(chat = updatedChat, messages = emptyList()) }
            println("ChatViewModel: Cleared history for chatId=$chatId")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка очистки истории"))
            println("ChatViewModel: Error clearing history: ${e.message}")
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            messagesChannel.unsubscribe()
            participantsChannel.unsubscribe()
            supabaseWrapper.realtime.removeChannel(messagesChannel)
            supabaseWrapper.realtime.removeChannel(participantsChannel)
            println("ChatViewModel: Unsubscribed from channels for chatId=$chatId")
        }
        super.onCleared()
    }
}