package com.kalltech.currenc_converter.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_update")
data class LastUpdateEntity(
    @PrimaryKey val id: Int = 0,
    val timestamp: Long
)
