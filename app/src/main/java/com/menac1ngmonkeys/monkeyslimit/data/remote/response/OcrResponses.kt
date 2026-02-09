package com.menac1ngmonkeys.monkeyslimit.data.remote.response

import com.google.gson.annotations.SerializedName

data class HealthResponse(
    val ok: Boolean,
    val status: String
)

data class OcrResponse(
    val ok: Boolean,
    val data: OcrData?,
    val error: String?
)

data class OcrData(
    val menu: List<OcrItem>,
    @SerializedName("sub_total") val subTotal: OcrSubTotal?,
    val total: OcrTotal?
)

data class OcrItem(
    val nm: String,
    val price_int: Double,
    val qty: Int
)

data class OcrSubTotal(
    @SerializedName("tax_price") val taxPrice: String?,
    @SerializedName("service_price") val servicePrice: String?,
    @SerializedName("discount_price") val discountPrice: String? // Assuming key based on typical structure
)

data class OcrTotal(
    @SerializedName("total_price") val totalPrice: String?
)