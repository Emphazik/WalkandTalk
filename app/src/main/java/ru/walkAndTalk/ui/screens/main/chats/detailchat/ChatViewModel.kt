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

    override val container = container<ChatViewState, ChatSideEffect>(ChatViewState(chatId = chatId, currentUserId = userId)) {
        loadChat()
        subscribeToMessages()
    }

    private lateinit var channel: RealtimeChannel // Сохраняем канал для отписки

    fun onIntent(intent: ChatIntent) = intent {
        when (intent) {
            is ChatIntent.UpdateInputText -> reduce { state.copy(inputText = intent.text) }
            is ChatIntent.SendMessage -> sendMessage(intent.chatId)
        }
    }

    private fun loadChat() = intent {
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

    private fun sendMessage(chatId: String) = intent {
        try {
            // Обрезаем пробелы и переносы строк
            val trimmedContent = state.inputText.trim()

            // Проверяем, не пустое ли сообщение
            if (trimmedContent.isEmpty()) {
                postSideEffect(ChatSideEffect.ShowError("Сообщение не может быть пустым"))
                println("ChatViewModel: Attempted to send empty message")
                return@intent
            }

            // Создаем временное сообщение с локальным ID
            val tempMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = userId,
                content = trimmedContent,
                createdAt = OffsetDateTime.now().toString(),
                isRead = true // Отправленные сообщения сразу помечаем как прочитанные
            )

            // Добавляем временное сообщение в локальный список
            reduce { state.copy(messages = state.messages + tempMessage, inputText = "") }
            println("ChatViewModel: Locally added temp message: $tempMessage")

            // Отправляем сообщение на сервер и получаем ответ
            val serverMessage = messagesRepository.sendMessage(chatId, userId, trimmedContent)
            println("ChatViewModel: Sent message to chatId=$chatId, server response: $serverMessage")

            // Обновляем локальный список с серверным ID
            val updatedMessages = state.messages.map {
                if (it.id == tempMessage.id) it.copy(id = serverMessage.id, isRead = true) else it
            }
            reduce { state.copy(messages = updatedMessages) }
            println("ChatViewModel: Updated message ID with server response: ${serverMessage.id}")
        } catch (e: Exception) {
            // Если отправка не удалась, откатываем локальное добавление
            reduce { state.copy(messages = state.messages.dropLast(1), inputText = state.inputText) }
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка отправки сообщения"))
            println("ChatViewModel: Error sending message: ${e.message}")
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

    private fun subscribeToMessages() = intent {
        try {
            channel = supabaseWrapper.realtime.channel("messages-channel-$chatId")

            val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                filter("chat_id", FilterOperator.EQ, chatId)
            }

            viewModelScope.launch {
                changeFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val message = Message(
                        id = record["id"]?.jsonPrimitive?.content ?: "",
                        chatId = record["chat_id"]?.jsonPrimitive?.content ?: "",
                        senderId = record["sender_id"]?.jsonPrimitive?.content ?: "",
                        content = record["content"]?.jsonPrimitive?.content ?: "",
                        createdAt = record["created_at"]?.jsonPrimitive?.content ?: "",
                        isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false
                    )

                    // Проверяем, не пустое ли сообщение
                    if (message.content.isEmpty()) {
                        println("ChatViewModel: Received empty message from Realtime, id=${message.id}")
                        return@collect
                    }

                    // Проверяем, не является ли это отправленным нами сообщением
                    if (state.messages.any { it.content == message.content && it.senderId == message.senderId }) {
                        println("ChatViewModel: Ignoring duplicate message from Realtime, id=${message.id}")
                        return@collect
                    }

                    reduce { state.copy(messages = state.messages + message) }
                    println("ChatViewModel: Received new message for chatId=$chatId, message=$message")
                    if (message.senderId != userId) {
                        markMessageAsRead(message)
                    }
                }
            }

            println("ChatViewModel: Attempting to subscribe to channel for chatId=$chatId")
            channel.subscribe()
            println("ChatViewModel: Successfully subscribed to messages for chatId=$chatId")
        } catch (e: Exception) {
            println("ChatViewModel: Error subscribing to messages: ${e.message}")
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

    override fun onCleared() {
        viewModelScope.launch {
            channel.unsubscribe()
            supabaseWrapper.realtime.removeChannel(channel)
            println("ChatViewModel: Unsubscribed from messages for chatId=$chatId")
        }
        super.onCleared()
    }
}