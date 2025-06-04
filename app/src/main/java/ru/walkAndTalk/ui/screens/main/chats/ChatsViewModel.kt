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
        loadChats(userId)
    }

    fun loadChats(userId: String) = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val chats = chatsRepository.fetchUserChats(userId)
            println("ChatsViewModel: Fetched ${chats.size} chats")
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
            chatCache.put(chat.id, chat)
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
            chatCache.put(chat.id, chat)
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
            postSideEffect(ChatsSideEffect.ShowError("Не удалось очистить историю чата. Пожалуйста, попробуйте еще раз или обратитесь в поддержку, если проблема сохраняется."))
        }
    }

    private suspend fun getUnreadCount(chatId: String, userId: String): Int? {
        return chatsRepository.getUnreadCount(chatId, userId)
    }
}