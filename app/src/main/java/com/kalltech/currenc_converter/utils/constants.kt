package com.kalltech.currenc_converter.utils

object Constants {
    const val EXCHANGE_RATE_API_BASE_URL = "https://v6.exchangerate-api.com/"
    const val FRANKFURTER_API_BASE_URL = "https://api.frankfurter.app/"
    const val API_KEY = "API-KEY" //needed for Exchangerate-api
    const val UPDATE_FREQUENCY_DAYS = 7L // Update frequency in days
    const val DATABASE_NAME = "currency_db"

    enum class ApiProvider {
        EXCHANGE_RATE_API {
            override fun toString(): String {
                return "Exchange Rate API"
            }
        },
        FRANKFURTER_API {
            override fun toString(): String {
                return "Frankfurter API"
            }
        }
    }

    // Set the default API provider here
    val DEFAULT_API_PROVIDER = ApiProvider.FRANKFURTER_API
    const val BASE_CURRENCY = "EUR" //Frankfurter uses EUR, ExchangeRate uses USD
}