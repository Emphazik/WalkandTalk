package ru.walkAndTalk.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadProfileImage(path: String, uri: Uri): Result<String>
    suspend fun uploadEventImage(path: String, uri: Uri): Result<String>
    suspend fun createSignedUrl(bucket: String, path: String): Result<String>
}