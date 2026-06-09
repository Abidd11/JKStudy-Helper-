package com.example.data.remote

import com.example.data.model.StudyMaterial
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AppUpdateConfig(
    @Json(name = "Maintenance") val maintenance: String = "false",
    @Json(name = "Maintenance msg") val maintenanceMsg: String = "",
    @Json(name = "version") val version: String = "1.0",
    @Json(name = "update msg") val updateMsg: String = "",
    @Json(name = "link") val link: String = ""
)

interface StudyApiService {
    @GET("macros/s/AKfycbwhqEZ6y873Ri8fidG2FC5KwDPJyYhMd8_AavwLiZg6EAcSeMUSYgJZj9WE5ZMAbFX8/exec")
    suspend fun getStudyMaterials(): List<StudyMaterial>

    @GET
    suspend fun getAppConfig(@Url url: String): List<AppUpdateConfig>

    @GET
    suspend fun getRawText(@Url url: String): Response<ResponseBody>

    companion object {
        private const val BASE_URL = "https://script.google.com/"

        fun create(): StudyApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Google Apps Script requires standard redirects and good timeout tolerances
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(StudyApiService::class.java)
        }
    }
}
