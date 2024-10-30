package com.kalltech.currenc_converter.utils

import android.content.Context
import com.kalltech.currenc_converter.model.Currency
import org.json.JSONObject
import java.io.IOException

object CurrencyUtils {
    fun loadCurrencies(context: Context): List<Currency> {
        val currencies = mutableListOf<Currency>()
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
                currencies.add(Currency(code, name, symbol))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return currencies.sortedBy { it.name }
    }
}
