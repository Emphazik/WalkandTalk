package ru.walkAndTalk.data.repository

import ru.walkAndTalk.data.mapper.toDomain
import ru.walkAndTalk.data.mapper.toDto
import ru.walkAndTalk.data.model.AnnouncementDto
import ru.walkAndTalk.data.model.EventDto
import ru.walkAndTalk.data.model.ReportDto
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Table
import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Report
import ru.walkAndTalk.domain.repository.AdminContentRepository

class AdminContentRepositoryImpl(
    private val supabaseWrapper: SupabaseWrapper
) : AdminContentRepository {
    override suspend fun fetchAllEvents(): List<Event> {
        val events = supabaseWrapper.postgrest[Table.EVENTS]
            .select()
            .decodeList<EventDto>()
        return events.map { eventDto ->
            val statusName = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
                .select { filter { eq("id", eventDto.statusId) } }
                .decodeSingle<Map<String, Any>>()["name"]?.toString() ?: "pending"
            eventDto.toDomain(statusName = statusName)
        }
    }

    override suspend fun fetchEventById(eventId: String): Event? {
        val eventDto = supabaseWrapper.postgrest[Table.EVENTS]
            .select { filter { eq("id", eventId) } }
            .decodeSingleOrNull<EventDto>() ?: return null
        val statusName = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("id", eventDto.statusId.toInt()) } }
            .decodeSingle<Map<String, Any>>()["name"]?.toString() ?: "pending"
        return eventDto.toDomain(statusName = statusName)
    }

    override suspend fun updateEvent(event: Event) {
        val statusId = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("name", event.status) } }
            .decodeSingle<Map<String, Any>>()["id"]?.toString()?.toInt()
            ?: throw Exception("Invalid status")
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
                .decodeSingle<Map<String, Any>>()["name"]?.toString() ?: "pending"
            announcementDto.toDomain(statusName = statusName)
        }
    }

    override suspend fun fetchAnnouncementById(announcementId: String): Announcement? {
        val announcementDto = supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
            .select { filter { eq("id", announcementId) } }
            .decodeSingleOrNull<AnnouncementDto>() ?: return null
        val statusName = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("id", announcementDto.statusId.toInt()) } }
            .decodeSingle<Map<String, Any>>()["name"]?.toString() ?: "pending"
        return announcementDto.toDomain(statusName = statusName)
    }

    override suspend fun updateAnnouncement(announcement: Announcement) {
        val statusId = supabaseWrapper.postgrest[Table.EVENT_STATUSES]
            .select { filter { eq("name", announcement.status) } }
            .decodeSingle<Map<String, Any>>()["id"]?.toString()?.toInt()
            ?: throw Exception("Invalid status")
        supabaseWrapper.postgrest[Table.ANNOUNCEMENTS]
            .update(
                mapOf(
                    "id" to announcement.id,
                    "title" to announcement.title,
                    "description" to announcement.description,
                    "creator_id" to announcement.creatorId,
                    "activity_type" to announcement.activityType,
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
                .decodeSingle<Map<String, Any>>()["name"]?.toString() ?: "unknown"
            reportDto.toDomain(contentTypeName = contentTypeName)
        }
    }

    override suspend fun fetchReportById(reportId: String): Report? {
        val reportDto = supabaseWrapper.postgrest[Table.REPORTS]
            .select { filter { eq("id", reportId) } }
            .decodeSingleOrNull<ReportDto>() ?: return null
        val contentTypeName = supabaseWrapper.postgrest[Table.CONTENT_TYPES]
            .select { filter { eq("id", reportDto.contentTypeId.toInt()) } }
            .decodeSingle<Map<String, Any>>()["name"]?.toString() ?: "unknown"
        return reportDto.toDomain(contentTypeName = contentTypeName)
    }

    override suspend fun deleteReport(reportId: String) {
        supabaseWrapper.postgrest[Table.REPORTS]
            .delete { filter { eq("id", reportId) } }
    }
}
