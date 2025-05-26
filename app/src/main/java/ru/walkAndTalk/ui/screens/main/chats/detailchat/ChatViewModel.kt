package ru.walkAndTalk.ui.screens.main.chats.detailchat

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import ru.walkAndTalk.domain.repository.ChatsRepository
import ru.walkAndTalk.domain.repository.MessagesRepository

class ChatViewModel(
    private val chatId: String,
    private val userId: String,
    private val chatsRepository: ChatsRepository,
    private val messagesRepository: MessagesRepository
) : ContainerHost<ChatViewState, ChatSideEffect>, ViewModel() {

    override val container = container<ChatViewState, ChatSideEffect>(ChatViewState(chatId = chatId, currentUserId = userId)) {
        loadChat()
    }

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
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, error = e.message) }
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка загрузки чата"))
            println("ChatViewModel: Error loading chat: ${e.message}")
        }
    }

    private fun sendMessage(chatId: String) = intent {
        try {
            messagesRepository.sendMessage(
                chatId = chatId,
                senderId = userId,
                content = state.inputText
            )
            reduce { state.copy(inputText = "") }
            loadChat()
            println("ChatViewModel: Sent message to chatId=$chatId")
        } catch (e: Exception) {
            postSideEffect(ChatSideEffect.ShowError(e.message ?: "Ошибка отправки сообщения"))
            println("ChatViewModel: Error sending message: ${e.message}")
        }
    }
}
