package com.menac1ngmonkeys.monkeyslimit.data.remote

import com.menac1ngmonkeys.monkeyslimit.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL = BuildConfig.BASE_URL

    fun getApiService(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().setLevel(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // 1. Connection Timeout: Time to find the server
            .connectTimeout(15, TimeUnit.SECONDS)

            // 2. Read Timeout: Time to wait for the AI to finish processing
            // Since Donut OCR is heavy, 60s is a safe "maximum"
            .readTimeout(180, TimeUnit.SECONDS)

            // 3. Write Timeout: Time to upload your image
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
