package ru.walkAndTalk.data.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import ru.walkAndTalk.BuildConfig

object SupabaseWrapper {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        defaultSerializer = KotlinXSerializer(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
        install(Auth)
        install(Storage)
        install(Realtime)
        install(Postgrest)
    }

    val auth = client.auth
    val storage = client.storage
    val realtime = client.realtime
    val postgrest = client.postgrest
}

//suspend fun authenticateWithSupabase(vkToken: String) {
//    try {
//        val response = SupabaseWrapper.postgrest["users"].insert(
//            mapOf(
//                "vk_token" to vkToken,
//                "created_at" to "now()"
//            )
//        )
//        Log.d("SUPABASE", "Пользователь добавлен: $response")
//    } catch (e: Exception) {
//        Log.e("SUPABASE", "Ошибка: ${e.message}")
//    }
//}
//
//@Serializable
//data class TestModel(
//    val id: Int,
//    val name: String
//)
//
//suspend fun checkConnection() {
//    try {
//        val response = SupabaseWrapper.postgrest.from("test_table").select().decodeList<TestModel>()
//        Log.d("Supabase", "Connection successful: $response")
//    } catch (e: Exception) {
//        Log.e("Supabase", "Connection failed: ${e.message}")
//    }
//}

