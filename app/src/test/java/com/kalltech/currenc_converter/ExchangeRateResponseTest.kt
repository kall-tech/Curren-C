package com.kalltech.currenc_converter

import com.kalltech.currenc_converter.network.ExchangeRateResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class ExchangeRateResponseTest {

    @Test
    fun testExchangeRateResponseParsing() {
        val jsonResponse = """
    {
        "result": "success",
        "documentation": "https://www.exchangerate-api.com/docs",
        "terms_of_use": "https://www.exchangerate-api.com/terms",
        "time_last_update_unix": 1585267200,
        "time_last_update_utc": "Fri, 27 Mar 2020 00:00:00 +0000",
        "time_next_update_unix": 1585353700,
        "time_next_update_utc": "Sat, 28 Mar 2020 00:00:00 +0000",
        "base_code": "USD",
        "conversion_rates": {
            "USD": 1,
            "AUD": 1.4817,
            "BGN": 1.7741,
            "CAD": 1.3168
        }
    }
    """
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(ExchangeRateResponse::class.java)
        val response = adapter.fromJson(jsonResponse)

        assertNotNull("Response should not be null", response)
        assertEquals("success", response?.result)
        assertEquals("USD", response?.baseCode)
        assertNotNull("Conversion rates should not be null", response?.conversionRates)
        assertTrue("Conversion rates should not be empty", response?.conversionRates?.isNotEmpty() == true)
        response?.conversionRates?.get("AUD")?.let { assertEquals(1.4817, it, 0.0001) }
    }

}
