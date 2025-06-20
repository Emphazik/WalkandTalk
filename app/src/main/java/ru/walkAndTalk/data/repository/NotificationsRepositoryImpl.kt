package ru.walkAndTalk.data.repository

import android.util.Log
import android.util.Log.e
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import ru.walkAndTalk.data.model.NotificationDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Notification
import ru.walkAndTalk.domain.repository.NotificationsRepository
import java.time.OffsetDateTime
import java.util.UUID

class NotificationsRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : NotificationsRepository {
    private var notificationsChannel: RealtimeChannel? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun fetchUserNotifications(userId: String): List<NotificationDto> {
        return supabaseWrapper.postgrest[Table.NOTIFICATIONS]
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<NotificationDto>()
    }

    override suspend fun markAsRead(notificationId: String) {
        supabaseWrapper.postgrest[Table.NOTIFICATIONS]
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("id", notificationId)
                }
            }
    }

    override suspend fun markAllAsRead(userId: String) {
        supabaseWrapper.postgrest[Table.NOTIFICATIONS]
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun subscribeToNotifications(
        userId: String,
        onUpdate: (NotificationDto) -> Unit
    ) {
        try {
            notificationsChannel = supabaseWrapper.realtime.channel("notifications-channel-$userId")
            val insertFlow = notificationsChannel!!.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = Table.NOTIFICATIONS
                filter("user_id", FilterOperator.EQ, userId)
            }
            val updateFlow = notificationsChannel!!.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                table = Table.NOTIFICATIONS
                filter("user_id", FilterOperator.EQ, userId)
            }

            coroutineScope.launch {
                insertFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val notification = NotificationDto(
                        id = record["id"]?.jsonPrimitive?.content ?: "",
                        userId = record["user_id"]?.jsonPrimitive?.content ?: "",
                        type = record["type"]?.jsonPrimitive?.content ?: "",
                        content = record["content"]?.jsonPrimitive?.content ?: "",
                        createdAt = record["created_at"]?.jsonPrimitive?.content ?: "",
                        isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false,
                        relatedId = record["related_id"]?.jsonPrimitive?.content
                    )
                    // Filter out notifications where the current user is the sender
                    if (notification.id.isNotEmpty() &&
                        notification.content.isNotEmpty() &&
                        !notification.content.contains("от $userId:")) {
                        onUpdate(notification)
                    }
                }
            }

            coroutineScope.launch {
                updateFlow.collect { payload ->
                    val record = payload.record as Map<String, JsonElement>
                    val notification = NotificationDto(
                        id = record["id"]?.jsonPrimitive?.content ?: "",
                        userId = record["user_id"]?.jsonPrimitive?.content ?: "",
                        type = record["type"]?.jsonPrimitive?.content ?: "",
                        content = record["content"]?.jsonPrimitive?.content ?: "",
                        createdAt = record["created_at"]?.jsonPrimitive?.content ?: "",
                        isRead = record["is_read"]?.jsonPrimitive?.boolean ?: false,
                        relatedId = record["related_id"]?.jsonPrimitive?.content
                    )
                    if (notification.id.isNotEmpty() &&
                        notification.content.isNotEmpty() &&
                        !notification.content.contains("от $userId:")) {
                        onUpdate(notification)
                    }
                }
            }

            notificationsChannel!!.subscribe()
            println("NotificationsRepositoryImpl: Subscribed to notifications for userId=$userId")
        } catch (e: Exception) {
            println("NotificationsRepositoryImpl: Error subscribing to notifications: ${e.message}")
            Log.e("NotificationsRepositoryImpl", "Subscription error", e)
        }
    }

    override suspend fun unsubscribeFromNotifications() {
        notificationsChannel?.unsubscribe()
        notificationsChannel = null
        println("NotificationsRepositoryImpl: Unsubscribed from notifications")
    }

    override suspend fun createNotifications(
        userIds: List<String>,
        type: String,
        content: String,
        relatedId: String?
    ): List<NotificationDto> {
        val notifications = userIds.map { userId ->
            NotificationDto(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = type,
                content = content,
                createdAt = OffsetDateTime.now().toString(),
                isRead = false,
                relatedId = relatedId
            )
        }
        try {
            notifications.forEach { notification ->
                supabaseWrapper.postgrest.from(Table.NOTIFICATIONS).insert(notification)
            }
            return notifications
        } catch (e: Exception) {
            println("NotificationsRepositoryImpl: Error inserting notifications: ${e.message}")
            throw e // Rethrow to handle in ChatViewModel
        }
    }
}
