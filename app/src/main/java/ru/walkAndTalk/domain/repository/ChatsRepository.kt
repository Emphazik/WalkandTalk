package ru.walkAndTalk.domain.repository
import ru.walkAndTalk.domain.model.Chat
import ru.walkAndTalk.domain.model.Message
import ru.walkAndTalk.domain.model.User

interface ChatsRepository {
    suspend fun fetchUserChats(userId: String): List<Chat>
    suspend fun fetchChatById(id: String): Chat?
    suspend fun createGroupChat(eventId: String, userId: String): Chat
    suspend fun createPrivateChat(userId1: String, userId2: String): Chat
    suspend fun getUnreadCount(chatId: String, userId: String): Int?
    suspend fun fetchUsersForChats(chatIds: List<String>): List<User> // Новый метод
    suspend fun findOrCreatePrivateChat(userId1: String, userId2: String): Chat // Новый метод
    suspend fun toggleMuteChat(chatId: String, mute: Boolean) // Новое
    suspend fun markChatAsRead(chatId: String, userId: String) // Новое
    suspend fun deleteChat(chatId: String) // Новое
    suspend fun clearChatHistory(chatId: String, userId: String)
    suspend fun findGroupChatByEventId(eventId: String): Chat?
    suspend fun removeUserFromChat(chatId: String, userId: String)

    suspend fun fetchMessages(chatId: String): List<Message>
}