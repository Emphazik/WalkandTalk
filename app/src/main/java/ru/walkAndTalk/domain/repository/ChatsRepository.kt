package ru.walkAndTalk.domain.repository
import ru.walkAndTalk.domain.model.Chat

interface ChatsRepository {
    suspend fun fetchUserChats(): List<Chat>
    suspend fun fetchChatById(id: String): Chat?
}