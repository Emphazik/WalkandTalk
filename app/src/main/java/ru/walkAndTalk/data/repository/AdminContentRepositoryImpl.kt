package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.data.model.ContentTypeDto
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.model.EventStatusDto
import ru.walkAndTalk.data.model.ReportDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Report
import ru.walkAndTalk.domain.model.User
import ru.walkAndTalk.domain.repository.AdminContentRepository
import java.time.Instant
import java.util.UUID

class AdminContentRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : AdminContentRepository {

//    override suspend fun fetchAllEvents(): List<Event> {
//        val events = supabaseWrapper.postgrest[Table.EVENTS]
//            .select()
//            .decodeList<EventDto>()
//        return events.map { eventDto ->
//            val status = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
//                .select { filter { eq("id", eventDto.statusId) } }
//                .decodeSingle<EventStatusDto>()
//            eventDto.toDomain(statusName = status.name)
//        }
//    }
//
//    override suspend fun fetchEventById(eventId: String): Event? {
//        val eventDto = supabaseWrapper.postgrest[Table.EVENTS]
//            .select { filter { eq("id", eventId) } }
//            .decodeSingleOrNull<EventDto>() ?: return null
//        val statusName = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
//            .select { filter { eq("id", eventDto.statusId.toInt()) } }
//            .decodeSingle<EventStatusDto>().name
//        return eventDto.toDomain(statusName = statusName)
//    }

    override suspend fun fetchAllEvents(): List<Event> {
        val events = supabaseWrapper.postgrest[Table.EVENTS]
            .select()
            .decodeList<EventDto>()
        return events.map { eventDto ->
            eventDto.toDomain() // Убрано statusName, так как toDomain не принимает этот параметр
        }
    }

    override suspend fun fetchEventById(eventId: String): Event? {
        val eventDto = supabaseWrapper.postgrest[Table.EVENTS]
            .select { filter { eq("id", eventId) } }
            .decodeSingleOrNull<EventDto>() ?: return null
        return eventDto.toDomain() // Убрано statusName и toInt()
    }

    override suspend fun updateEvent(event: Event) {
        val statusId = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("name", event.statusId) } }
            .decodeSingle<EventStatusDto>().id
        supabaseWrapper.postgrest[Table.EVENTS]
            .update(
                mapOf(
                    "id" to event.id,
                    "creator_id" to event.creatorId,
                    "title" to event.title,
                    "description" to event.description,
                    "location" to event.location,
                    "event_date" to event.eventDate,
                    "created_at" to event.createdAt,
                    "event_image_url" to event.eventImageUrl,
                    "tag_ids" to event.tagIds,
                    "status_id" to statusId
                )
            ) {
                filter { eq("id", event.id) }
            }
    }

    override suspend fun deleteEvent(eventId: String) {
        supabaseWrapper.postgrest[Table.EVENTS]
            .delete { filter { eq("id", eventId) } }
    }

    override suspend fun fetchAllAnnouncements(): List<Announcement> {
        val announcements = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
            .select()
            .decodeList<AnnouncementDto>()
        return announcements.map { announcementDto ->
            val statusName = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
                .select { filter { eq("id", announcementDto.statusId.toInt()) } }
                .decodeSingle<EventStatusDto>()
            announcementDto.toDomain()
        }
    }

    override suspend fun fetchAnnouncementById(announcementId: String): Announcement? {
        val announcementDto = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
            .select { filter { eq("id", announcementId) } }
            .decodeSingleOrNull<AnnouncementDto>() ?: return null
        val statusName = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("id", announcementDto.statusId.toInt()) } }
            .decodeSingle<EventStatusDto>()
        return announcementDto.toDomain()
    }

    override suspend fun updateAnnouncement(announcement: Announcement) {
        val statusId = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("name", announcement.statusId) } }
            .decodeSingle<EventStatusDto>().id
        supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
            .update(
                mapOf(
                    "id" to announcement.id,
                    "title" to announcement.title,
                    "description" to announcement.description,
                    "creator_id" to announcement.creatorId,
                    "activity_type" to announcement.activityTypeId,
                    "status_id" to statusId,
                    "created_at" to announcement.createdAt,
                    "updated_at" to announcement.updatedAt
                )
            ) {
                filter { eq("id", announcement.id) }
            }
    }

    override suspend fun deleteAnnouncement(announcementId: String) {
        supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
            .delete { filter { eq("id", announcementId) } }
    }

    override suspend fun fetchAllReports(): List<Report> {
        val reports = supabaseWrapper.postgrest[Table.REPORTS]
            .select()
            .decodeList<ReportDto>()
        return reports.map { reportDto ->
            val contentTypeName = supabaseWrapper.postgrest[Table.CONTENT_TYPES]
                .select { filter { eq("id", reportDto.contentTypeId.toInt()) } }
                .decodeSingle<ContentTypeDto>().name
            reportDto.toDomain(contentTypeName = contentTypeName)
        }
    }

    override suspend fun fetchReportById(reportId: String): Report? {
        val reportDto = supabaseWrapper.postgrest[Table.REPORTS]
            .select { filter { eq("id", reportId) } }
            .decodeSingleOrNull<ReportDto>() ?: return null
        val contentTypeName = supabaseWrapper.postgrest[Table.CONTENT_TYPES]
            .select { filter { eq("id", reportDto.contentTypeId.toInt()) } }
            .decodeSingle<ContentTypeDto>().name

        return reportDto.toDomain(contentTypeName = contentTypeName)
    }

    override suspend fun deleteReport(reportId: String) {
        supabaseWrapper.postgrest[Table.REPORTS]
            .delete { filter { eq("id", reportId) } }
    }

    override suspend fun addUser(name: String, email: String, password: String) {
        supabaseWrapper.postgrest[Table.USERS].insert(
            mapOf(
                "id" to "user-${UUID.randomUUID()}",
                "name" to name,
                "email" to email,
                "password" to password, // Пароль должен быть захеширован
                "phone" to "", // Пустое значение по умолчанию
                "profile_image_url" to "",
                "created_at" to Instant.now().toString(),
                "is_admin" to false
            )
        )
    }

//    override suspend fun updateUser(user: User) {
//        supabaseWrapper.postgrest[Table.USERS]
//            .update(
//                mapOf(
//                    "name" to user.name,
//                    "email" to user.email
//                )
//            ) {
//                filter { eq("id", user.id) }
//            }
//    }
}
