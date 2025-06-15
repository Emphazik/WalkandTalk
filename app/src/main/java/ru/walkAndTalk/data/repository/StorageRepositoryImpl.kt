package ru.walkAndTalk.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.walkAndTalk.data.network.SupabaseWrapper
import ru.walkAndTalk.domain.Bucket
import ru.walkAndTalk.domain.repository.StorageRepository
import java.io.InputStream
import kotlin.time.Duration.Companion.days


class StorageRepositoryImpl(
    private val context: Context,
    private val supabaseWrapper: SupabaseWrapper
) : StorageRepository {

    override suspend fun uploadProfileImage(path: String, uri: Uri): Result<String> {
        return upload(Bucket.PROFILE_IMAGES, path, uri)
    }

    override suspend fun uploadEventImage(path: String, uri: Uri): Result<String> {
        return upload(Bucket.EVENTS_IMAGES, path, uri)
    }

    private suspend fun upload(bucket: String, path: String, uri: Uri): Result<String> {
        return try {
            val byteArray = uri.toByteArray(context)
            withContext(Dispatchers.IO) {
                supabaseWrapper.storage[bucket].upload(path = path, data = byteArray) {
                    upsert = true
                }
            }
            val publicUrl = supabaseWrapper.storage[bucket].publicUrl(path = path)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Uri.toByteArray(context: Context): ByteArray {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(this)
        return inputStream?.use { it.readBytes() } ?: byteArrayOf()
    }

    override suspend fun createSignedUrl(bucketId: String, path: String): Result<String> {
        return try {
            val signedUrl = supabaseWrapper.storage[bucketId].createSignedUrl(
                path = path,
                expiresIn = 365.days // 1 год
            )
            Result.success(signedUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
