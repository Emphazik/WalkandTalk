package ru.walkAndTalk.domain

object Regex {
    val EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    val PHONE = "^\\+7\\d{10}\$".toRegex()
}