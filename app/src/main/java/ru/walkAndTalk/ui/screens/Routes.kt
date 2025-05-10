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

/** Local Main Screen **/
// Screen
@Serializable data class Profile(val userId: String)
@Serializable object Feed
@Serializable object Chats
@Serializable object Search

