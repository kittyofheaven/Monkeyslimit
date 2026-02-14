package com.menac1ngmonkeys.monkeyslimit.data.remote

import androidx.annotation.Keep
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.ClassifyRequest
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.ClassifyResponse
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.HealthResponse
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.OcrResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@Keep
interface ApiService {
    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    @Multipart
    @POST("predict")
    suspend fun predictReceipt(
        @Part file: MultipartBody.Part
    ): Response<OcrResponse>

    @POST("classify")
    suspend fun classifyText(
        @Body request: ClassifyRequest
    ): Response<ClassifyResponse>
}