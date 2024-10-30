package com.kalltech.currenc_converter

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
import com.kalltech.currenc_converter.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var amountEditTexts: List<EditText>
    private lateinit var currencyAutoCompleteTexts: List<AutoCompleteTextView>
    private lateinit var lastUpdateTextView: TextView
    private lateinit var rootView: View
    private lateinit var ratesInfoTextView: TextView



    private val viewModel: MainViewModel by viewModels {
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()

        val repository = ExchangeRateRepository(
            ApiClient.apiService,
            database.exchangeRateDao(),
            database.lastUpdateDao()
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
        super.onCreate(savedInstanceState)
        // Using setContentView with data binding
        setContentView(R.layout.activity_main)

        rootView = findViewById(android.R.id.content)

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

        lastUpdateTextView = findViewById(R.id.lastUpdateTextView)
        ratesInfoTextView = findViewById(R.id.ratesInfoTextView)

        setupCurrencySpinners()
        setupAmountEditTexts()
        observeViewModel()
    }

    private fun setupCurrencySpinners() {
    Log.d(TAG, "setupCurrencySpinners() called")
    val currencyList = CurrencyUtils.loadCurrencies(this)

    val adapter = ArrayAdapter(
        this,
        android.R.layout.simple_dropdown_item_1line,
        currencyList.map { "${it.code} - ${it.name}" }
    )

    for (i in currencyAutoCompleteTexts.indices) {
        currencyAutoCompleteTexts[i].setAdapter(adapter)

        // Set default currency
        val selectedCurrencyCode = viewModel.selectedCurrencies.value?.get(i)?.code ?: "USD"
        val position = currencyList.indexOfFirst { it.code == selectedCurrencyCode }
        if (position >= 0) {
            currencyAutoCompleteTexts[i].setText(
                "${currencyList[position].code} - ${currencyList[position].name}",
                false
            )
        }

        currencyAutoCompleteTexts[i].setOnItemClickListener { parent, view, position, id ->
            val currency = currencyList[position]
            Log.d(TAG, "Currency selected: ${currency.code}")
            viewModel.onCurrencyChanged(i, currency)
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
            ratesInfoTextView.text = info
        })

        viewModel.errorMessage.observe(this, Observer { message ->
            Log.e(TAG, "Error: $message")
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
        })
    }
}
