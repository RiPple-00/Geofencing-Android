package com.example.geofencing.di

import com.example.geofencing.BuildConfig
import com.example.geofencing.data.remote.supabase.SupabaseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

// Supabase(PostgREST) 전용 Retrofit/OkHttp — 팀 BE(NetworkModule)와 별개 인스턴스.
// 모든 요청에 apikey + Authorization(Bearer anon key) 헤더를 인터셉터로 붙인다.
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseApi(json: Json): SupabaseApi {
        val key = BuildConfig.SUPABASE_ANON_KEY
        val client = OkHttpClient.Builder()
            // 모든 Supabase 요청에 인증 헤더를 자동으로 붙이는 인터셉터.
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", key)
                    .addHeader("Authorization", "Bearer $key")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
        val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/') + "/"
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create()
    }
}
