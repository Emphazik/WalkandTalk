package ru.walkAndTalk.domain.repository

import ru.walkAndTalk.domain.model.Event
import ru.walkAndTalk.domain.model.Announcement
import ru.walkAndTalk.domain.model.Report

interface AdminContentRepository {
    suspend fun fetchAllEvents(): List<Event>
    suspend fun fetchEventById(eventId: String): Event?
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(eventId: String)
    suspend fun fetchAllAnnouncements(): List<Announcement>
    suspend fun fetchAnnouncementById(announcementId: String): Announcement?
    suspend fun updateAnnouncement(announcement: Announcement)
    suspend fun deleteAnnouncement(announcementId: String)
    suspend fun fetchAllReports(): List<Report>
    suspend fun fetchReportById(reportId: String): Report?
    suspend fun deleteReport(reportId: String)
}