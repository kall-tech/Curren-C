package com.kalltech.currenc_converter.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterApiService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") baseCurrency: String? = null
    ): Response<FrankfurterApiResponse>
}