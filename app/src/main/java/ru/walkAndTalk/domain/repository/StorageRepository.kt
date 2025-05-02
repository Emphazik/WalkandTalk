package ru.walkAndTalk.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadProfileImage(path: String, uri: Uri)
    suspend fun upload(bucket: String, path: String, uri: Uri)
    suspend fun createSignedUrl(bucket: String, path: String): String
}