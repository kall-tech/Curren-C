package com.kalltech.currenc_converter.utils

import android.content.Context
import com.kalltech.currenc_converter.model.Currency
import org.json.JSONObject
import java.io.IOException

object CurrencyUtils {
    fun loadCurrencyMap(context: Context): Map<String, Currency> { //mapping is more efficient
        val currencyMap = mutableMapOf<String, Currency>()
        try {
            val jsonString = context.assets.open("currencies.json")
                .bufferedReader()
                .use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val code = keys.next()
                val currencyObject = jsonObject.getJSONObject(code)
                val name = currencyObject.getString("name")
                val symbol = currencyObject.optString("symbol", "")
                val currency = Currency(code, name, symbol)
                currencyMap[code] = currency
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return currencyMap
    }
}
