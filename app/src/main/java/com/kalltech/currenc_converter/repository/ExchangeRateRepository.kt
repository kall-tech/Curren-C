package com.kalltech.currenc_converter.repository


import com.kalltech.currenc_converter.database.ExchangeRateDao
import com.kalltech.currenc_converter.database.ExchangeRateEntity
import com.kalltech.currenc_converter.database.LastUpdateDao
import com.kalltech.currenc_converter.database.LastUpdateEntity
import com.kalltech.currenc_converter.network.ExchangeRateApiService
import com.kalltech.currenc_converter.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import android.util.Log

class ExchangeRateRepository(
    private val apiService: ExchangeRateApiService,
    private val exchangeRateDao: ExchangeRateDao,
    private val lastUpdateDao: LastUpdateDao

) {
    private suspend fun fetchAndSaveExchangeRates(baseCurrency: String): Result<Unit> {
    return try {
        val response = apiService.getLatestRates(Constants.API_KEY, baseCurrency)
        if (response.isSuccessful) {
            Log.d("ExchangeRateRepository", "API call successful")
            val body = response.body()
            if (body != null && body.result == "success") {
                val rates = body.conversionRates?.map {
                    ExchangeRateEntity(it.key, it.value)
                } ?: emptyList()
                exchangeRateDao.insertRates(rates)
                lastUpdateDao.insertLastUpdate(LastUpdateEntity(timestamp = System.currentTimeMillis()))
                Result.success(Unit)
            } else {
                Log.e("ExchangeRateRepository", "API call failed: ${response.errorBody()?.string()}")
                Result.failure(Exception("API Error"))
            }
        } else {
            Result.failure(Exception("Network Error"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
    suspend fun updateExchangeRates(baseCurrency: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
                val lastUpdate = lastUpdateDao.getLastUpdate()
                val currentTime = System.currentTimeMillis()
                val updateFrequencyMillis = TimeUnit.DAYS.toMillis(Constants.UPDATE_FREQUENCY_DAYS)

                val needsUpdate = lastUpdate == null || (currentTime - lastUpdate.timestamp) > updateFrequencyMillis

                //val needsUpdate = true //for debugging
                if (needsUpdate) {
                    Log.d("ExchangeRateRepository", "Exchange Rates older than a week! Updating exchange rates...")
                    fetchAndSaveExchangeRates(baseCurrency)
                } else {
                    Result.success(Unit)
                }

        }
    }

    suspend fun forceUpdateExchangeRates(baseCurrency: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            fetchAndSaveExchangeRates(baseCurrency) // Call fetchAndSaveRates here with "USD"
        }
    }


    suspend fun getCachedRates(): List<ExchangeRateEntity> {
        return withContext(Dispatchers.IO) {
            val rates = exchangeRateDao.getAllRates()
            logExchangeRates(rates) // Log the rates
            rates
        }
    }

    private fun logExchangeRates(rates: List<ExchangeRateEntity>) {
        Log.d("ExchangeRateRepository", "Cached Exchange Rates:")
        for (rate in rates) {
            Log.d("ExchangeRateRepository", "  ${rate.currencyCode}: ${rate.rate}")
        }
    }

    suspend fun getLastUpdateTime(): Long? {
        return withContext(Dispatchers.IO) {
            lastUpdateDao.getLastUpdate()?.timestamp
        }
    }
}
