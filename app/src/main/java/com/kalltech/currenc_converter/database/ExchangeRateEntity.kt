package com.kalltech.currenc_converter.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val currencyCode: String,
    val rate: Double
)
