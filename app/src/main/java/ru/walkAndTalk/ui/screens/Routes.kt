package ru.walkAndTalk.ui.screens

import kotlinx.serialization.Serializable

/** Global **/
// Screen
@Serializable object Splash
// Screen
@Serializable object Onboarding
// Flow
@Serializable object Auth
// Nested
@Serializable object Welcome
@Serializable object Login
@Serializable object Registration
// Screen
@Serializable
data class Main(val userId: String)

// Nested
@Serializable data class Admin(val userId: String)
/** Local Main Screen **/
// Screen
@Serializable
data class Profile(val userId: String, val viewOnly: Boolean = false)
@Serializable object Feed
@Serializable object Chats
@Serializable object Search
@Serializable data class EventDetails(val eventId: String){
    companion object {
        const val ROUTE = "eventDetails/{eventId}"
        fun createRoute(eventId: String) = "eventDetails/$eventId"
    }
}
@Serializable data class AnnouncementDetails(val announcementId: String) {
    companion object {
        const val ROUTE = "announcement_details/{announcementId}"
        fun createRoute(announcementId: String) = "announcement_details/$announcementId"
    }
}
@Serializable data class EditEvent(val eventId: String) {
    companion object {
        const val ROUTE = "edit_event/{eventId}"
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
}
@Serializable data class EditAnnouncement(val announcementId: String) {
    companion object {
        const val ROUTE = "edit_announcement/{announcementId}"
        fun createRoute(announcementId: String) = "edit_announcement/$announcementId"
    }
}
@Serializable object EditProfile
@Serializable object EventStatistics
@Serializable object Notifications
@Serializable object AddUser
@Serializable object EditUser
