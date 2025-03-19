package ru.walkAndTalk.data.network

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

class SupabaseWrapper {

    private val client = createSupabaseClient(
        supabaseUrl = "https://tvecrsehuuqrjwjfgljf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR2ZWNyc2VodXVxcmp3amZnbGpmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg2NzY2NDAsImV4cCI6MjA1NDI1MjY0MH0.5dL8BozqX8J_7FIeZSmuv6D_HrSo05lL66oIjiK0Zxo"
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