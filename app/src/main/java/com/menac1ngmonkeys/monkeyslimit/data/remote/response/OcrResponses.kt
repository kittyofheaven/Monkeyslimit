package com.menac1ngmonkeys.monkeyslimit.data.remote.response

import androidx.annotation.Keep
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

@Keep
data class HealthResponse(
    val ok: Boolean = false,
    val status: String = ""
)

@Keep
data class OcrResponse(
    val ok: Boolean = false,
    val data: OcrData? = null,
    val error: String? = null
)

@Keep
data class OcrData(
    val menu: JsonElement? = null,
    @SerializedName("sub_total") val subTotal: OcrSubTotal? = null,
    val total: OcrTotal? = null
)

@Keep
data class OcrItem(
    val nm: String = "",
    val price_int: Double = 0.0,
    val qty: Int = 0
)

@Keep
data class OcrSubTotal(
    @SerializedName("tax_price") val taxPrice: String? = null,
    @SerializedName("service_price") val servicePrice: String? = null,
    @SerializedName("discount_price") val discountPrice: String? = null // Assuming key based on typical structure
)

@Keep
data class OcrTotal(
    @SerializedName("total_price") val totalPrice: String? = null
)

// --- NEW: Classification Models ---

@Keep
data class ClassifyRequest(
    val text: String = ""
)

@Keep
data class ClassifyResponse(
    val ok: Boolean = false,
    val data: ClassifyData? = null,
    val error: String? = null
)

@Keep
data class ClassifyData(
    val prediction: String = ""
)