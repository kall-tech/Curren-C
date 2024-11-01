package com.kalltech.currenc_converter.network


//import androidx.compose.foundation.layout.add
//import androidx.privacysandbox.tools.core.generator.build
import com.kalltech.currenc_converter.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    val exchangeRateApiService: ExchangeRateApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.EXCHANGE_RATE_API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ExchangeRateApiService::class.java)
    }

    // New FrankfurterApiService
    val frankfurterApiService: FrankfurterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.FRANKFURTER_API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FrankfurterApiService::class.java)
    }
}
