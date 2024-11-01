package com.kalltech.currenc_converter.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.kalltech.currenc_converter.model.Currency
import java.util.*

class CurrencyAutoCompleteAdapter(
    context: Context,
    private var currencyList: List<Currency>
) : ArrayAdapter<Currency>(context, android.R.layout.simple_dropdown_item_1line, currencyList) {

    private var filteredCurrencyList: List<Currency> = currencyList

    override fun getCount(): Int {
        return filteredCurrencyList.size
    }

    override fun getItem(position: Int): Currency? {
        return filteredCurrencyList[position]
    }

    override fun getFilter(): Filter {
        return currencyFilter
    }

    private val currencyFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (!constraint.isNullOrEmpty()) {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                val filteredList = currencyList.filter {
                    it.code.lowercase(Locale.getDefault()).contains(filterPattern) ||
                            it.name.lowercase(Locale.getDefault()).contains(filterPattern)
                }
                filterResults.values = filteredList
                filterResults.count = filteredList.size
            } else {
                filterResults.values = currencyList
                filterResults.count = currencyList.size
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredCurrencyList = if (results?.values != null) {
                @Suppress("UNCHECKED_CAST")
                results.values as List<Currency>
            } else {
                emptyList()
            }
            notifyDataSetChanged()
        }
    }

    // This method returns the view for the text field (after selection)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val currency = getItem(position)
        (view as TextView).text = currency?.code // Display only the ISO code
        return view
    }

    // This method returns the view for the dropdown items
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val currency = getItem(position)
        (view as TextView).text = "${currency?.code} - ${currency?.name}" // Display code and name
        return view
    }

    // Function to update the currency list
    fun updateCurrencyList(newCurrencyList: List<Currency>) {
        this.currencyList = newCurrencyList
        this.filteredCurrencyList = newCurrencyList
        notifyDataSetChanged()
    }
}
