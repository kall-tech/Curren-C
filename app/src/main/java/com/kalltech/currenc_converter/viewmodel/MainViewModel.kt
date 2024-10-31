package com.kalltech.currenc_converter.viewmodel

import androidx.lifecycle.*
import com.kalltech.currenc_converter.database.ExchangeRateEntity
import com.kalltech.currenc_converter.model.Currency
import com.kalltech.currenc_converter.repository.ExchangeRateRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(private val repository: ExchangeRateRepository) : ViewModel() {

    val inputAmounts = MutableLiveData<List<String>>(listOf("", "", ""))
    val selectedCurrencies = MutableLiveData<List<Currency>>()
    val convertedAmounts = MutableLiveData<List<String>>(listOf("", "", ""))
    val lastUpdateTime = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()
    val ratesInfo = MutableLiveData<String>()
    private var lastEditedIndex: Int = 0 // Default to first field


    private var exchangeRates: List<ExchangeRateEntity> = emptyList()

    init {
        // Set default currencies
        selectedCurrencies.value = listOf(
            Currency("EUR", "Euro", "€"),
            Currency("USD", "United States Dollar", "$"),
            Currency("AUD", "Australian Dollar", "AU$")
        )

        // Load exchange rates
        viewModelScope.launch {
            val baseCurrency = "USD" // You can change the base currency as needed
            val result = repository.updateExchangeRates(baseCurrency)
            if (result.isFailure) {
                errorMessage.postValue("Failed to update rates.")
            } else {
                exchangeRates = repository.getCachedRates()
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

        val amounts = inputAmounts.value ?: listOf("", "", "")
        val lastAmountStr = amounts[lastEditedIndex]

        if (lastAmountStr.isNotBlank() && lastAmountStr.toDoubleOrNull() != null) {
            val lastAmount = lastAmountStr.toDouble()
            calculateConversions(lastEditedIndex, lastAmount)
        }
    }


    private fun calculateConversions(inputIndex: Int, amount: Double) {
         if (exchangeRates.isEmpty()) {
                errorMessage.postValue("Exchange rates not available.")
                return
         }
        val currencies = selectedCurrencies.value ?: return
        val baseCurrencyCode = currencies[inputIndex].code
        val baseRate = exchangeRates.find { it.currencyCode == baseCurrencyCode }?.rate ?: return
        if (baseRate == null) {
            errorMessage.postValue("Exchange rate not available for $baseCurrencyCode.")
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
        lastUpdateTime.postValue("Rates last updated: $formattedTime")
    }

    fun forceUpdateExchangeRates() {
        viewModelScope.launch {
            isUpdating.postValue(true)
            val result = repository.forceUpdateExchangeRates()
            if (result.isFailure) {
                errorMessage.postValue("Failed to update rates.")
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
            }
            isUpdating.postValue(false)
        }
    }

    // Add LiveData to track updating state
    val isUpdating = MutableLiveData<Boolean>(false)

}
