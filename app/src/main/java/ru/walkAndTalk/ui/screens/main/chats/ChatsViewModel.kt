package ru.walkAndTalk.ui.screens.main.chats

import androidx.collection.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.data.model.ChatParticipantDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository
import java.time.OffsetDateTime

//class ChatsViewModel(
//    private val chatsRepository: ChatsRepository,
//    private val eventsRepository: EventsRepository,
//    private val userId: String,
//    private val supabaseWrapper: SupabaseWrapper
//) : ViewModel(), ContainerHost<ChatsViewState, ChatsSideEffect> {
//
//    override val container: Container<ChatsViewState, ChatsSideEffect> = container(ChatsViewState())
//    private val chatCache = LruCache<String, List<Chat>>(1)
//
//    init {
//        loadChats(userId)
//    }
//

class ChatsViewModel(
    private val chatsRepository: ChatsRepository,
    private val eventsRepository: EventsRepository,
    private val userId: String,
    private val supabaseWrapper: SupabaseWrapper
) : ViewModel(), ContainerHost<ChatsViewState, ChatsSideEffect> {

    override val container: Container<ChatsViewState, ChatsSideEffect> = container(ChatsViewState()) {
        loadChats(userId)
        subscribeToNewMessages()
    }

    private val chatCache = LruCache<String, List<Chat>>(1)
    private lateinit var messagesChannel: RealtimeChannel
    private var isSubscribed = false

    private fun subscribeToNewMessages() = intent {
        if (isSubscribed) {
            println("ChatsViewModel: Already subscribed to messages")
            return@intent
        }
        try {
            messagesChannel = supabaseWrapper.realtime.channel("chats-messages-global")
            val insertFlow = messagesChannel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "messages"
            }
            viewModelScope.launch {
                insertFlow.collect { payload ->
                    println("ChatsViewModel: Received new message payload: $payload")
                    val record = payload.record as Map<String, JsonElement>
                    val chatId = record["chat_id"]?.jsonPrimitive?.content ?: ""
                    val isUserInChat = supabaseWrapper.postgrest.from("chat_participants")
                        .select {
                            filter { eq("chat_id", chatId); eq("user_id", userId) }
                        }
                        .decodeList<ChatParticipantDto>()
                        .isNotEmpty()
                    if (isUserInChat) {
                        chatCache.evictAll()
                        val chats = chatsRepository.fetchUserChats(userId)
                            .sortedByDescending { it.lastMessageTime?.let { time -> OffsetDateTime.parse(time).toInstant().toEpochMilli() } ?: 0L }
                        chatCache.put(userId, chats)
                        reduce { state.copy(chats = chats) }
                        println("ChatsViewModel: Updated chats due to new message in chatId=$chatId")
                    }
                }
            }
            messagesChannel.subscribe()
            isSubscribed = true
            println("ChatsViewModel: Successfully subscribed to messages")
        } catch (e: Exception) {
            println("ChatsViewModel: Error subscribing to messages: ${e.message}")
            postSideEffect(ChatsSideEffect.ShowError("Failed to subscribe to message updates"))
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            if (::messagesChannel.isInitialized) {
                messagesChannel.unsubscribe()
                supabaseWrapper.realtime.removeChannel(messagesChannel)
                isSubscribed = false
                println("ChatsViewModel: Unsubscribed from messages channel")
            }
        }
        super.onCleared()
    }


    fun loadChats(userId: String) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val cachedChats = chatCache.get(userId)
            if (cachedChats != null) {
                println("ChatsViewModel: Loaded ${cachedChats.size} chats from cache")
                reduce { state.copy(chats = cachedChats, isLoading = false) }
                return@intent
            }
            val chats = chatsRepository.fetchUserChats(userId)
                .sortedByDescending { it.lastMessageTime?.let { time -> OffsetDateTime.parse(time).toInstant().toEpochMilli() } ?: 0L }
            chatCache.put(userId, chats)
            println("ChatsViewModel: Fetched ${chats.size} chats, cached for userId=$userId")
            reduce { state.copy(chats = chats, isLoading = false) }
        } catch (e: Exception) {
            println("ChatsViewModel: LoadChats error=${e.message}, stackTrace=${e.stackTraceToString()}")
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка загрузки чатов"))
        }
    }


    fun refreshChats() = intent {
        chatCache.evictAll()
        println("ChatsViewModel: Cleared cache, refreshing chats")
        loadChats(userId)
    }

    fun onChatClick(chatId: String) = intent {
        postSideEffect(ChatsSideEffect.NavigateToChat(chatId))
        chatCache.remove(chatId)
        println("ChatsViewModel: Cleared cache for chat id=$chatId before navigation")
    }

    fun startPrivateChat(otherUserId: String) = intent {
        try {
            val chat = chatsRepository.createPrivateChat(userId, otherUserId)
            chatCache.put(chat.id, listOf(chat))
            println("ChatsViewModel: Created private chat id=${chat.id} with userId=$otherUserId")
            postSideEffect(ChatsSideEffect.NavigateToChat(chat.id))
            loadChats(userId)
        } catch (e: Exception) {
            println("ChatsViewModel: CreatePrivateChat error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка создания чата"))
        }
    }

    fun createGroupChat(eventId: String) = intent {
        try {
            val chat = chatsRepository.createGroupChat(eventId, userId)
            chatCache.put(chat.id, listOf(chat))
            println("ChatsViewModel: Created group chat id=${chat.id} for eventId=$eventId")
            postSideEffect(ChatsSideEffect.NavigateToChat(chat.id))
            loadChats(userId)
        } catch (e: Exception) {
            println("ChatsViewModel: CreateGroupChat error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка создания чата"))
        }
    }

    fun toggleSearch() = intent {
        reduce { state.copy(isSearchActive = !state.isSearchActive, searchQuery = if (state.isSearchActive) "" else state.searchQuery) }
    }

    fun updateSearchQuery(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
    }

    fun toggleMuteChat(chatId: String, mute: Boolean) = intent {
        chatsRepository.toggleMuteChat(chatId, mute)
        val updatedChats = state.chats.map {
            if (it.id == chatId) it.copy(isMuted = mute) else it
        }
        reduce { state.copy(chats = updatedChats) }
    }

    fun markChatAsRead(chatId: String) = intent {
        chatsRepository.markChatAsRead(chatId, userId)
        val updatedChats = state.chats.map {
            if (it.id == chatId) it.copy(unreadCount = 0) else it
        }
        reduce { state.copy(chats = updatedChats) }
    }

    fun deleteChat(chatId: String) = intent {
        try {
            chatsRepository.deleteChat(chatId)
            val updatedChats = state.chats.filter { it.id != chatId }
            reduce { state.copy(chats = updatedChats) }
            chatCache.remove(chatId)
            println("ChatsViewModel: Deleted chat id=$chatId")
        } catch (e: Exception) {
            println("ChatsViewModel: DeleteChat error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка удаления чата"))
        }
    }

    fun leaveGroupChat(chatId: String) = intent {
        try {
            chatsRepository.leaveGroupChat(chatId, userId)
            val updatedChats = state.chats.filter { it.id != chatId }
            reduce { state.copy(chats = updatedChats) }
            chatCache.remove(chatId)
            println("ChatsViewModel: Left group chat id=$chatId")
            postSideEffect(ChatsSideEffect.ShowSuccess("Вы вышли из группового чата"))
        } catch (e: Exception) {
            println("ChatsViewModel: LeaveGroupChat error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка выхода из чата"))
        }
    }

    fun clearChatHistory(chatId: String) = intent {
        try {
            chatsRepository.clearChatHistory(chatId, userId)
            val updatedChats = state.chats.map {
                if (it.id == chatId) {
                    it.copy(lastMessage = null, lastMessageTime = null, unreadCount = getUnreadCount(chatId, userId))
                } else it
            }
            reduce { state.copy(chats = updatedChats) }
            println("ChatsViewModel: Cleared history for chat id=$chatId")
        } catch (e: Exception) {
            println("ChatsViewModel: ClearChatHistory error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError("Не удалось очистить историю чата"))
        }
    }

    fun getLastMessageText(chat: Chat, userId: String): String {
        val lastMessage = chat.lastMessage ?: return "Нет сообщений"
        return if (chat.type == "group") {
            if (chat.lastMessageSenderId == userId) {
                "Вы: $lastMessage"
            } else {
                val senderName = chat.participantName ?: "Неизвестный"
                "$senderName: $lastMessage"
            }
        } else {
            if (chat.lastMessageSenderId == userId) "Вы: $lastMessage" else lastMessage
        }
    }

    private suspend fun getUnreadCount(chatId: String, userId: String): Int? {
        return chatsRepository.getUnreadCount(chatId, userId)
    }
}