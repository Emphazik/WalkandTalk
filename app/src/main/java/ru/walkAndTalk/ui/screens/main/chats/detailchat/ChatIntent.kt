package ru.walkAndTalk.ui.screens.main.chats.detailchat

sealed class ChatIntent {
    data class UpdateInputText(val text: String) : ChatIntent()
    data class SendMessage(val chatId: String) : ChatIntent()
    data class DeleteMessages(val messageIds: List<String>, val isLocal: Boolean = false) : ChatIntent() // Добавили isLocal
    data class EditMessage(val messageId: String, val newContent: String) : ChatIntent()
    data class ToggleMessageSelection(val messageId: String) : ChatIntent()
    class ClearSelection : ChatIntent()
    data class ShowCopySuccess(val message: String = "Текст скопирован") : ChatIntent()
}