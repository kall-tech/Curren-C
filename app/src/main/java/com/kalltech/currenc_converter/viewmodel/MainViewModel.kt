package com.kalltech.currenc_converter.viewmodel

import androidx.lifecycle.*
import com.kalltech.currenc_converter.database.ExchangeRateEntity
import com.kalltech.currenc_converter.model.Currency
import com.kalltech.currenc_converter.repository.ExchangeRateRepository
import com.kalltech.currenc_converter.utils.Constants
import com.kalltech.currenc_converter.utils.CurrencyUtils
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val repository: ExchangeRateRepository,
                    private val currencyMap: Map<String, Currency>) : ViewModel() {

    val inputAmounts = MutableLiveData<List<String>>(listOf("", "", ""))
    val selectedCurrencies = MutableLiveData<List<Currency>>()
    val convertedAmounts = MutableLiveData<List<String>>(listOf("", "", ""))
    val lastUpdateTime = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()
    val successMessage = MutableLiveData<String>()
    val ratesInfo = MutableLiveData<String>()
    private var lastEditedIndex: Int = 0 // Default to first field
    val availableCurrencies = MutableLiveData<List<Currency>>()
    val baseCurrency = Constants.BASE_CURRENCY // You can change the base currency as needed


    private var exchangeRates: List<ExchangeRateEntity> = emptyList()

    init {
        // Set default currencies -> now in MainActivity

        // Load exchange rates
        viewModelScope.launch {

            val result = repository.updateExchangeRates(baseCurrency)
            if (result.isFailure) {
                errorMessage.postValue("Failed to update rates from ${Constants.DEFAULT_API_PROVIDER.toString()}.")
            } else {
                exchangeRates = repository.getCachedRates()
                // Get available currency codes from the repository
                val currencyCodes = repository.getAvailableCurrencyCodes()

                // Map currency codes to Currency objects (using names and symbols)
                val currencyList = currencyCodes.map { code ->
                    currencyMap[code] ?: Currency(code, code, "") // Use code as name if not found
                }
                // Update LiveData
                availableCurrencies.postValue(currencyList)
                updateLastUpdateTime()
                // Update rates info
                val ratesSummary = exchangeRates.joinToString(separator = "\n") {
                    "${it.currencyCode}: ${it.rate}"
                }
                ratesInfo.postValue(ratesSummary)
            }
        }
    }

    fun onAmountChanged(index: Int, amount: String) {
        val amounts = inputAmounts.value?.toMutableList() ?: mutableListOf("", "", "")
        amounts[index] = amount
        inputAmounts.value = amounts
        if (amount.isNotBlank() && amount.toDoubleOrNull() != null) {
            lastEditedIndex = index // Update last edited index
            calculateConversions(index, amount.toDouble())
        } else {
            errorMessage.postValue("Please enter a valid number.")
        }
    }

    fun onCurrencyChanged(index: Int, currency: Currency) {
        val currencies = selectedCurrencies.value?.toMutableList() ?: mutableListOf()
        currencies[index] = currency
        selectedCurrencies.value = currencies
        //saveSelectedCurrencies(currencies)
        val amounts = inputAmounts.value ?: listOf("", "", "")
        val lastAmountStr = amounts[lastEditedIndex]

        if (lastAmountStr.isNotBlank() && lastAmountStr.toDoubleOrNull() != null) {
            val lastAmount = lastAmountStr.toDouble()
            calculateConversions(lastEditedIndex, lastAmount)
        }
    }


    private fun calculateConversions(inputIndex: Int, amount: Double) {
         if (exchangeRates.isEmpty()) {
                errorMessage.postValue("Exchange rates not available at ${Constants.DEFAULT_API_PROVIDER.toString()}.")
                return
         }
        val currencies = selectedCurrencies.value ?: return
        val baseCurrencyCode = currencies[inputIndex].code
        val baseRate = exchangeRates.find { it.currencyCode == baseCurrencyCode }?.rate ?: return
        if (baseRate == null) {
            errorMessage.postValue("Exchange rate not available for $baseCurrencyCode at ${Constants.DEFAULT_API_PROVIDER.toString()}.")
            return
        }
        val newAmounts = mutableListOf<String>("", "", "")

        for (i in 0..2) {
            if (i == inputIndex) {
                newAmounts[i] = amount.toString()
            } else {
                val targetCurrencyCode = currencies[i].code
                val targetRate = exchangeRates.find { it.currencyCode == targetCurrencyCode }?.rate ?: continue
            if (targetRate != null) {
                val convertedAmount = (amount / baseRate) * targetRate
                val roundedAmount = BigDecimal(convertedAmount).setScale(2, RoundingMode.HALF_EVEN)
                newAmounts[i] = roundedAmount.toPlainString()
            } else {
                newAmounts[i] = "N/A"
            }
        }
        convertedAmounts.postValue(newAmounts)
    }
    }

    private suspend fun updateLastUpdateTime() {
        val timestamp = repository.getLastUpdateTime() ?: return
        val formattedTime = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        lastUpdateTime.postValue("Rates last updated: ${formattedTime},\nProvider: ${Constants.DEFAULT_API_PROVIDER.toString()}")
    }

    fun forceUpdateExchangeRates() {
        viewModelScope.launch {
            isUpdating.postValue(true)
            val result = repository.forceUpdateExchangeRates(baseCurrency)
            if (result.isFailure) {
                errorMessage.postValue("Failed to force update rates from ${Constants.DEFAULT_API_PROVIDER}.")
            } else {
                exchangeRates = repository.getCachedRates()
                updateLastUpdateTime()
                // Recalculate conversions if needed
                val amounts = inputAmounts.value ?: listOf("", "", "")
                amounts.forEachIndexed { index, amount ->
                    if (amount.isNotBlank() && amount.toDoubleOrNull() != null) {
                        calculateConversions(index, amount.toDouble())
                        return@forEachIndexed
                    }
                }
                successMessage.postValue("Exchange rates force updated successfully from ${Constants.DEFAULT_API_PROVIDER.toString()}.")
            }
            isUpdating.postValue(false)

        }
    }

    // Add LiveData to track updating state
    val isUpdating = MutableLiveData<Boolean>(false)

}
