package ru.walkAndTalk.domain.repository
import ru.walkAndTalk.domain.model.Chat

interface ChatsRepository {
    suspend fun fetchUserChats(userId: String): List<Chat>
    suspend fun fetchChatById(id: String): Chat?
    suspend fun createGroupChat(eventId: String, userId: String): Chat
    suspend fun createPrivateChat(userId1: String, userId2: String): Chat
    suspend fun getUnreadCount(chatId: String, userId: String): Int?
}