package ru.walkAndTalk.ui.screens.main.chats

import androidx.collection.LruCache
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository

class ChatsViewModel(
    private val chatsRepository: ChatsRepository,
    private val eventsRepository: EventsRepository,
    private val userId: String
) : ViewModel(), ContainerHost<ChatsViewState, ChatsSideEffect> {

    override val container: Container<ChatsViewState, ChatsSideEffect> = container(ChatsViewState())
    private val chatCache = LruCache<String, Chat>(100)

    init {
        loadChats(
            userId = userId
        )
    }

    fun loadChats(userId: String) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val chats = chatsRepository.fetchUserChats(userId)
            println("ChatsViewModel: Fetched ${chats.size} chats: $chats")
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
        loadChats(userId = userId)
    }

    fun onChatClick(chatId: String) = intent {
        val cachedChat = chatCache.get(chatId)
        if (cachedChat != null) {
            println("ChatsViewModel: Using cached chat id=$chatId")
            postSideEffect(ChatsSideEffect.NavigateToChat(chatId))
            // Асинхронное обновление кэша
            try {
                val updatedChat = chatsRepository.fetchChatById(chatId)
                if (updatedChat != null) {
                    chatCache.put(chatId, updatedChat)
                    println("ChatsViewModel: Asynchronously updated cache for chat id=$chatId")
                    reduce { state.copy(chats = state.chats.map { if (it.id == chatId) updatedChat else it }) }
                }
            } catch (e: Exception) {
                println("ChatsViewModel: Async update error for chat id=$chatId, error=${e.message}")
            }
        } else {
            try {
                val chat = chatsRepository.fetchChatById(chatId)
                if (chat != null) {
                    chatCache.put(chatId, chat)
                    println("ChatsViewModel: Fetched and cached chat id=$chatId")
                    postSideEffect(ChatsSideEffect.NavigateToChat(chatId))
                } else {
                    postSideEffect(ChatsSideEffect.ShowError("Ошибка загрузки чата"))
                }
            } catch (e: Exception) {
                println("ChatsViewModel: FetchChat error=${e.message}")
                postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка загрузки чата"))
            }
        }
    }

    fun startPrivateChat(otherUserId: String) = intent {
        try {
            val chat = chatsRepository.createPrivateChat(userId, otherUserId)
            chatCache.put(chat.id, chat)
            println("ChatsViewModel: Created private chat id=${chat.id} with userId=$otherUserId")
            postSideEffect(ChatsSideEffect.NavigateToChat(chat.id))
            loadChats(userId = userId)
        } catch (e: Exception) {
            println("ChatsViewModel: CreatePrivateChat error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка создания чата"))
        }
    }

    fun createGroupChat(eventId: String) = intent {
        try {
            val chat = chatsRepository.createGroupChat(eventId, userId)
            chatCache.put(chat.id, chat)
            println("ChatsViewModel: Created group chat id=${chat.id} for eventId=$eventId")
            postSideEffect(ChatsSideEffect.NavigateToChat(chat.id))
            loadChats(userId = userId)
        } catch (e: Exception) {
            println("ChatsViewModel: CreateGroupChat error=${e.message}")
            postSideEffect(ChatsSideEffect.ShowError(e.message ?: "Ошибка создания чата"))
        }
    }
}