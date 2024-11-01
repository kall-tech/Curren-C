package com.kalltech.currenc_converter.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates")
    suspend fun getAllRates(): List<ExchangeRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)

    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAllRates()

    @Transaction
    suspend fun replaceAllRates(rates: List<ExchangeRateEntity>) {
        deleteAllRates()
        insertRates(rates)
    }
}
