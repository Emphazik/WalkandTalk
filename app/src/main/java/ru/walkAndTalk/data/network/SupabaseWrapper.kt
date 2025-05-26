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

class SupabaseWrapper {

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

