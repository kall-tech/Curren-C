package com.kalltech.currenc_converter.network

import com.squareup.moshi.Json

data class ExchangeRateResponse(
    val result: String,
    val documentation: String?,
    @Json(name = "terms_of_use")
    val termsOfUse: String?,
    @Json(name = "time_last_update_unix")
    val timeLastUpdateUnix: Long,
    @Json(name = "time_last_update_utc")
    val timeLastUpdateUtc: String?,
    @Json(name = "time_next_update_unix")
    val timeNextUpdateUnix: Long?,
    @Json(name = "time_next_update_utc")
    val timeNextUpdateUtc: String?,
    @Json(name = "base_code")
    val baseCode: String,
    @Json(name = "conversion_rates")
    val conversionRates: Map<String, Double>?
)

data class ErrorResponse(
    val result: String,
    @Json(name = "error-type")
    val errorType: String
)
