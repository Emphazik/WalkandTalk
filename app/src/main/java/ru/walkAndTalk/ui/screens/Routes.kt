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
@Serializable data class EventDetails(val eventId: String)
//{
////    companion object {
////        const val ROUTE = "eventDetails/{eventId}"
////        fun createRoute(eventId: String) = "eventDetails/$eventId"
////    }
//}
@Serializable data class AnnouncementDetails(val announcementId: String)
@Serializable data class EditEvent(val eventId: String)
@Serializable data class EditAnnouncement(val announcementId: String)
@Serializable object EditProfile
@Serializable object EventStatistics
@Serializable object Notifications
@Serializable object AddUser
@Serializable data class EditUser(val userId: String)
