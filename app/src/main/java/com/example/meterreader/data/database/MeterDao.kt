package com.example.meterreader.data.database

import androidx.room.*
import com.example.meterreader.data.model.MeterData
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterDao {
    @Query("SELECT * FROM meters WHERE objectId = :objectId")
    fun getMetersByObject(objectId: Long): Flow<List<MeterData>>

    @Insert
    suspend fun insert(meter: MeterData)

    @Update
    suspend fun update(meter: MeterData)

    @Delete
    suspend fun delete(meter: MeterData)

    @Query("SELECT * FROM meters WHERE id = :id")
    suspend fun getMeterById(id: Long): MeterData?
}
