package ru.walkAndTalk.ui.screens.main.chats

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.EventsRepository

class ChatsViewModel(
    private val chatsRepository: ChatsRepository,
    private val eventsRepository: EventsRepository
) : ViewModel(), ContainerHost<ChatsViewState, ChatsSideEffect> {

    override val container: Container<ChatsViewState, ChatsSideEffect> = container(ChatsViewState())

    init {
        loadChats()
    }

    private fun loadChats() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        try {
            val chats = chatsRepository.fetchUserChats()
            reduce { state.copy(chats = chats, isLoading = false) }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
        }
    }

    fun onChatClick(chatId: String) = intent {
        postSideEffect(ChatsSideEffect.NavigateToChat(chatId))
    }
}