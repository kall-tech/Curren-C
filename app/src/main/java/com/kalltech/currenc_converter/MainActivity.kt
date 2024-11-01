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
import com.google.android.material.textfield.TextInputLayout
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





    private val viewModel: MainViewModel by viewModels {
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
                    return MainViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        ThemeUtils.applyTheme(ThemeUtils.getSavedThemePref(this))
        super.onCreate(savedInstanceState)
        // Using setContentView with data binding
        setContentView(R.layout.activity_main)

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

        setupCurrencySpinners()
        setupAmountEditTexts()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun setupCurrencySpinners() {
        Log.d(TAG, "setupCurrencySpinners() called")
        val currencyList = CurrencyUtils.loadCurrencies(this)
        Log.d(TAG, "Number of currencies loaded: ${currencyList.size}")

        val adapter = CurrencyAutoCompleteAdapter(this, currencyList)

        for (i in currencyAutoCompleteTexts.indices) {
            currencyAutoCompleteTexts[i].setAdapter(adapter)

            // Set default currency
            val selectedCurrencyCode = viewModel.selectedCurrencies.value?.get(i)?.code ?: "USD"
            val defaultCurrency = currencyList.find { it.code == selectedCurrencyCode }
            if (defaultCurrency != null) {
                currencyAutoCompleteTexts[i].setText(defaultCurrency.code, false)
                // Update the symbol display
                //currencySymbolTextViews[i].text = "${defaultCurrency.symbol} (${defaultCurrency.code})"
                currencySymbolTextViews[i].text = defaultCurrency.symbol
            }

            currencyAutoCompleteTexts[i].setOnItemClickListener { parent, view, position, id ->
                val selectedCurrency = adapter.getItem(position)
                if (selectedCurrency != null) {
                    viewModel.onCurrencyChanged(i, selectedCurrency)
                    Log.d(TAG, "Currency selected: ${selectedCurrency.code}")

                    // Update the symbol display
                    //currencySymbolTextViews[i].text = "${selectedCurrency.symbol} (${selectedCurrency.code})"
                    currencySymbolTextViews[i].text = selectedCurrency.symbol

                    //update the CurrencyAutoCompleteTextView
                    currencyAutoCompleteTexts[i].setText(selectedCurrency.code, false)
                }
                // Set initial symbol
                //val initialCurrency = currencyList[position]
                //currencySymbolTextViews[i].text =
                    //"${initialCurrency.symbol} (${initialCurrency.code})"
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
                        viewModel.onAmountChanged(i, s.toString())
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
