package ru.walkAndTalk.domain.model

data class Chat(

    //ПЕРЕДЕЛАТЬ ХУЙНЯ
    // SUPABASE - надо переделать таблицы для логики чата
    val id: String,
    val eventId: String,
    val eventName: String, // Название мероприятия
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: String?
)