package com.kalltech.currenc_converter

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.kalltech.currenc_converter.database.AppDatabase
import com.kalltech.currenc_converter.model.Currency
import android.widget.AutoCompleteTextView
import com.kalltech.currenc_converter.network.ApiClient
import com.kalltech.currenc_converter.repository.ExchangeRateRepository
import com.kalltech.currenc_converter.utils.Constants
import com.kalltech.currenc_converter.utils.CurrencyUtils
import com.kalltech.currenc_converter.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.kalltech.currenc_converter.adapter.CurrencyAutoCompleteAdapter
import com.kalltech.currenc_converter.utils.ThemeConstants
import com.kalltech.currenc_converter.utils.ThemeUtils


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var amountEditTexts: List<EditText>
    private lateinit var currencyAutoCompleteTexts: List<AutoCompleteTextView>
    private lateinit var lastUpdateTextView: TextView
    private lateinit var rootView: View
    private lateinit var ratesInfoTextView: TextView
    private lateinit var refreshButton: ImageButton
    private lateinit var themeToggleButton: ImageButton
    private lateinit var currencySymbolTextViews: List<TextView>
    private lateinit var currencyAdapters: List<CurrencyAutoCompleteAdapter>
    private lateinit var currencyMap: Map<String, Currency>

    companion object { // for saving the currencies
        private const val PREFS_NAME = "CurrencyPrefs"
        private const val KEY_CURRENCY_1 = "selected_currency_1"
        private const val KEY_CURRENCY_2 = "selected_currency_2"
        private const val KEY_CURRENCY_3 = "selected_currency_3"
    }



    private val viewModel: MainViewModel by viewModels {
        currencyMap = CurrencyUtils.loadCurrencyMap(this)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()

        val repository = ExchangeRateRepository(
            ApiClient.exchangeRateApiService,
            ApiClient.frankfurterApiService,
            database.exchangeRateDao(),
            database.lastUpdateDao(),
            Constants.DEFAULT_API_PROVIDER
        )

        object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(repository, currencyMap) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private fun saveSelectedCurrencies(currencyCodes: List<String>) {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(KEY_CURRENCY_1, currencyCodes.getOrNull(0) ?: "")
            putString(KEY_CURRENCY_2, currencyCodes.getOrNull(1) ?: "")
            putString(KEY_CURRENCY_3, currencyCodes.getOrNull(2) ?: "")
            apply()
        }
    }

    private fun loadSelectedCurrencies(): List<String> {
        val sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val c1 = sharedPref.getString(KEY_CURRENCY_1, "EUR") ?: "EUR"
        val c2 = sharedPref.getString(KEY_CURRENCY_2, "USD") ?: "USD"
        val c3 = sharedPref.getString(KEY_CURRENCY_3, "AUD") ?: "AUD"
        return listOf(c1, c2, c3)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        ThemeUtils.applyTheme(ThemeUtils.getSavedThemePref(this))
        super.onCreate(savedInstanceState)
        // Using setContentView with data binding
        setContentView(R.layout.activity_main)

        currencyMap = CurrencyUtils.loadCurrencyMap(this)

        rootView = findViewById(android.R.id.content)
    // Textviews here
        amountEditTexts = listOf(
            findViewById(R.id.amountEditText1),
            findViewById(R.id.amountEditText2),
            findViewById(R.id.amountEditText3)
        )

        currencyAutoCompleteTexts = listOf(
            findViewById(R.id.currencyAutoComplete1),
            findViewById(R.id.currencyAutoComplete2),
            findViewById(R.id.currencyAutoComplete3)
        )

        currencySymbolTextViews = listOf(
            findViewById(R.id.currencySymbolTextView1),
            findViewById(R.id.currencySymbolTextView2),
            findViewById(R.id.currencySymbolTextView3)
        )

        // Load the previously selected currencies from SharedPreferences
        val lastSelectedCodes = loadSelectedCurrencies()

        // Convert them to Currency objects
        val lastSelectedCurrencies = lastSelectedCodes.map { code ->
            currencyMap[code] ?: Currency(code, code, "")
        }

        // Update the ViewModel's selected currencies
        viewModel.selectedCurrencies.value = lastSelectedCurrencies

        lastUpdateTextView = findViewById(R.id.lastUpdateTextView)
        //buttons here
        refreshButton = findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            viewModel.forceUpdateExchangeRates()
        }
        themeToggleButton = findViewById(R.id.themeToggleButton)
        themeToggleButton.setOnClickListener {
            cycleTheme()
        }

        ratesInfoTextView = findViewById(R.id.ratesInfoTextView)

        viewModel.availableCurrencies.observe(this, Observer { currencyList ->
            setupCurrencySpinners(currencyList)
        })
        setupAmountEditTexts()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun setupCurrencySpinners(currencyList: List<Currency>) {
        Log.d(TAG, "setupCurrencySpinners() called")
        Log.d(TAG, "Number of currencies passed: ${currencyList.size}")
        // Create adapters if not initialized
        if (!::currencyAdapters.isInitialized) {
            // First-time setup
            currencyAdapters = currencyAutoCompleteTexts.map {
                CurrencyAutoCompleteAdapter(this, currencyList)
            }

            for (i in currencyAutoCompleteTexts.indices) {
                val autoCompleteTextView = currencyAutoCompleteTexts[i]
                autoCompleteTextView.setAdapter(currencyAdapters[i])

                // Set default or previously selected currency
                val selectedCurrency = viewModel.selectedCurrencies.value?.get(i)
                if (selectedCurrency != null && currencyList.contains(selectedCurrency)) {
                    autoCompleteTextView.setText(selectedCurrency.code, false)
                    currencySymbolTextViews[i].text = selectedCurrency.symbol
                } else {
                    // Select default currency (e.g., first in list)
                    val defaultCurrency = currencyList.firstOrNull()
                    if (defaultCurrency != null) {
                        autoCompleteTextView.setText(defaultCurrency.code, false)
                        viewModel.onCurrencyChanged(i, defaultCurrency)
                    }
                }

                autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                    val selectedCurrency = currencyAdapters[i].getItem(position)
                    if (selectedCurrency != null) {
                        viewModel.onCurrencyChanged(i, selectedCurrency)
                        Log.d(TAG, "Currency selected: ${selectedCurrency.code}")
                        // Update currency symbol display if needed
                        currencySymbolTextViews[i].text = selectedCurrency.symbol
                        autoCompleteTextView.setText(selectedCurrency.code, false)

                        // Save updated selections to SharedPreferences
                        val allSelectedCodes = currencyAutoCompleteTexts.map { it.text.toString() }
                        saveSelectedCurrencies(allSelectedCodes)
                    }
                }
            }
        } else {
            // If adapters are already initialized, update them
            currencyAdapters.forEach { adapter ->
                adapter.updateCurrencyList(currencyList)
            }

            // Update AutoCompleteTextViews with the new data
            for (i in currencyAutoCompleteTexts.indices) {
                val autoCompleteTextView = currencyAutoCompleteTexts[i]
                val selectedCurrency = viewModel.selectedCurrencies.value?.get(i)
                if (selectedCurrency != null && currencyList.contains(selectedCurrency)) {
                    // Keep current selection
                    autoCompleteTextView.setText(selectedCurrency.code, false)
                } else {
                    // Select default currency
                    val defaultCurrency = currencyList.firstOrNull()
                    if (defaultCurrency != null) {
                        autoCompleteTextView.setText(defaultCurrency.code, false)
                        viewModel.onCurrencyChanged(i, defaultCurrency)
                    }
                    // Set initial symbol
                    //val initialCurrency = currencyList[position]
                    //currencySymbolTextViews[i].text =
                    //"${initialCurrency.symbol} (${initialCurrency.code})"
                }
            }
        }
    }


    private fun setupAmountEditTexts() {
        for (i in amountEditTexts.indices) {
            amountEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (amountEditTexts[i].hasFocus()) {
                    // Replace commas with dots
                    val newText = s.toString().replace(',', '.')

                    // If the text actually changed, update it (this avoids infinite recursion)
                    if (newText != s.toString()) {
                        val cursorPosition = amountEditTexts[i].selectionStart
                        amountEditTexts[i].removeTextChangedListener(this)
                        amountEditTexts[i].setText(newText)
                        // Move cursor to the end or to the old position if it’s shorter
                        amountEditTexts[i].setSelection(minOf(cursorPosition, newText.length))
                        amountEditTexts[i].addTextChangedListener(this)
                    } else {
                        // Normal numeric checks
                        viewModel.onAmountChanged(i, newText)
                    }
                }
            }
        })
    }
}

    private fun cycleTheme() {
        val currentTheme = ThemeUtils.getSavedThemePref(this)
        val newTheme = when (currentTheme) {
            ThemeConstants.THEME_MODE_SYSTEM -> ThemeConstants.THEME_MODE_LIGHT
            ThemeConstants.THEME_MODE_LIGHT -> ThemeConstants.THEME_MODE_DARK
            ThemeConstants.THEME_MODE_DARK -> ThemeConstants.THEME_MODE_SYSTEM
            else -> ThemeConstants.THEME_MODE_SYSTEM
        }
        ThemeUtils.saveThemePref(this, newTheme)
        ThemeUtils.applyTheme(newTheme)
        recreate()
        // Optional: Show a message indicating the current theme
        val themeName = when (newTheme) {
            ThemeConstants.THEME_MODE_LIGHT -> "Light Mode"
            ThemeConstants.THEME_MODE_DARK -> "Dark Mode"
            else -> "System Default"
        }
        viewModel.successMessage.postValue("Theme switched to $themeName")
    }


    private fun observeViewModel() {
        Log.d(TAG, "observeViewModel() called")
        viewModel.convertedAmounts.observe(this, Observer { amounts ->
            for (i in amountEditTexts.indices) {
                if (!amountEditTexts[i].hasFocus()) {
                    amountEditTexts[i].setText(amounts[i])
                    amountEditTexts[i].alpha = 0f
                    amountEditTexts[i].animate().alpha(1f).setDuration(300).start()
                }
            }
        })

        viewModel.availableCurrencies.observe(this, Observer { currencyList ->
            setupCurrencySpinners(currencyList)
        })


        viewModel.lastUpdateTime.observe(this, Observer { timeString ->
            lastUpdateTextView.text = timeString
        })

        viewModel.ratesInfo.observe(this, Observer { info ->
            ratesInfoTextView.text = null//info
        })

        viewModel.errorMessage.observe(this, Observer { message ->
            Log.e(TAG, "Error: $message")
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
        })

        viewModel.successMessage.observe(this, Observer { message ->
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
        })


        viewModel.isUpdating.observe(this, Observer { updating ->
            refreshButton.isEnabled = !updating
            if (updating) {
                // Optionally show a loading indicator
                refreshButton.animate().rotationBy(360f).setDuration(1000).start()
            } else {
                refreshButton.animate().cancel()
                refreshButton.rotation = 0f
            }
        })

    }
}
