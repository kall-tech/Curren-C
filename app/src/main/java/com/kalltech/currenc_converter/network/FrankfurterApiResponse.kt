package com.kalltech.currenc_converter.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FrankfurterApiResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)