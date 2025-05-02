package ru.walkAndTalk.data.repository

import android.content.Context
import android.net.Uri
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Bucket
import ru.walkAndTalk.domain.repository.StorageRepository
import java.io.InputStream
import kotlin.time.Duration.Companion.seconds

class StorageRepositoryImpl(
    private val context: Context,
    private val supabaseWrapper: SupabaseWrapper
) : StorageRepository {

    override suspend fun uploadProfileImage(path: String, uri: Uri) {
        upload(Bucket.PROFILE_IMAGES, path, uri)
    }

    override suspend fun upload(bucket: String, path: String, uri: Uri) {
        supabaseWrapper.storage[bucket].upload(
            path = path,
            data = uri.toByteArray(context)
        ) { upsert = true }
    }

    private fun Uri.toByteArray(context: Context): ByteArray {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(this)
        return inputStream?.use { it.readBytes() } ?: byteArrayOf()
    }

    override suspend fun createSignedUrl(bucket: String, path: String): String {
        return supabaseWrapper.storage[bucket].createSignedUrl(path, expiresIn = (60 * 60 * 24).seconds)
    }
}