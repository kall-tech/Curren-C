package com.kalltech.currenc_converter.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExchangeRateEntity::class, LastUpdateEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun lastUpdateDao(): LastUpdateDao
}
