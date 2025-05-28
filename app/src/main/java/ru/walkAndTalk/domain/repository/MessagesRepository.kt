package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Message

interface MessagesRepository {
    suspend fun fetchMessages(chatId: String): List<Message>
    suspend fun sendMessage(chatId: String, senderId: String, content: String): Message // Изменено на возврат Message
    suspend fun markMessageAsRead(messageId: String)
    suspend fun deleteMessages(messageIds: List<String>)
    suspend fun editMessage(messageId: String, newContent: String): Message // Новый метод для редактирования
}