package com.kalltech.currenc_converter.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LastUpdateDao {
    @Query("SELECT * FROM last_update WHERE id = 0")
    suspend fun getLastUpdate(): LastUpdateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastUpdate(lastUpdate: LastUpdateEntity)
}
