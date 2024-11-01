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
import com.kalltech.currenc_converter.network.ExchangeRateResponse
import com.kalltech.currenc_converter.network.FrankfurterApiResponse
import com.kalltech.currenc_converter.network.FrankfurterApiService

class ExchangeRateRepository(
    private val exchangeRateApiService: ExchangeRateApiService,
    private val frankfurterApiService: FrankfurterApiService,
    private val exchangeRateDao: ExchangeRateDao,
    private val lastUpdateDao: LastUpdateDao,
    private val apiProvider: Constants.ApiProvider = Constants.DEFAULT_API_PROVIDER
) {
    private var availableCurrencyCodes: List<String> = emptyList()

    fun getAvailableCurrencyCodes(): List<String> {
        return availableCurrencyCodes
    }


    private suspend fun fetchAndSaveExchangeRates(baseCurrency: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                when (apiProvider) {
                    Constants.ApiProvider.EXCHANGE_RATE_API -> {
                        val response = exchangeRateApiService.getLatestRates(Constants.API_KEY, baseCurrency)
                        handleExchangeRateApiResponse(response)
                    }
                    Constants.ApiProvider.FRANKFURTER_API -> {
                        val base = if (baseCurrency == "EUR") null else baseCurrency
                        val response = frankfurterApiService.getLatestRates(base)
                        handleFrankfurterApiResponse(response)
                    }
                }
            } catch (e: Exception) {
                Log.e("ExchangeRateRepository", "Error fetching exchange rates", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun handleExchangeRateApiResponse(
        response: retrofit2.Response<ExchangeRateResponse>
    ): Result<Unit> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.result == "success") {
                val rates = body.conversionRates?.map {
                    ExchangeRateEntity(it.key, it.value)
                } ?: emptyList()
                exchangeRateDao.insertRates(rates)
                lastUpdateDao.insertLastUpdate(LastUpdateEntity(timestamp = System.currentTimeMillis()))
                // Store available currency codes
                availableCurrencyCodes = rates.map { it.currencyCode }
                // Remove duplicates
                availableCurrencyCodes = availableCurrencyCodes.distinct()
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error"))
            }
        } else {
            Result.failure(Exception("Network Error"))
        }
    }

    private suspend fun handleFrankfurterApiResponse(
        response: retrofit2.Response<FrankfurterApiResponse>
    ): Result<Unit> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                val rates = body.rates.map {
                    ExchangeRateEntity(it.key, it.value)
                }
                // Include the base currency with rate 1.0
                val baseRate = ExchangeRateEntity(body.base, 1.0)
                exchangeRateDao.insertRates(rates + baseRate)
                lastUpdateDao.insertLastUpdate(LastUpdateEntity(timestamp = System.currentTimeMillis()))
                // Store available currency codes
                availableCurrencyCodes = rates.map { it.currencyCode }
                // Include the base currency
                availableCurrencyCodes = availableCurrencyCodes + body.base
                // Remove duplicates
                availableCurrencyCodes = availableCurrencyCodes.distinct()
                Result.success(Unit)
            } else {
                Result.failure(Exception("API Error"))
            }
        } else {
            Result.failure(Exception("Network Error"))
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
            availableCurrencyCodes = rates.map { it.currencyCode }
            rates

        }
    }

    private fun logExchangeRates(rates: List<ExchangeRateEntity>) {
        Log.d("ExchangeRateRepository", "${rates.size} Cached Exchange Rates:")
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
