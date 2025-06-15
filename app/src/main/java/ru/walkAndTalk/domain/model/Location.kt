package ru.walkAndTalk.domain.model

data class Location(
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)