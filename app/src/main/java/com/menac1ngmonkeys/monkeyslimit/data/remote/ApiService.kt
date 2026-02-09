package com.menac1ngmonkeys.monkeyslimit.data.remote

import com.menac1ngmonkeys.monkeyslimit.data.remote.response.HealthResponse
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.OcrResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    @Multipart
    @POST("predict")
    suspend fun predictReceipt(
        @Part file: MultipartBody.Part
    ): Response<OcrResponse>
}