package com.example.meterreader.data.database

import androidx.room.*
import com.example.meterreader.data.model.ReadingData
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE meterId = :meterId ORDER BY date ASC")
    fun getReadingsByMeter(meterId: Long): Flow<List<ReadingData>>

    @Insert
    suspend fun insert(reading: ReadingData)

    @Update
    suspend fun update(reading: ReadingData)

    @Delete
    suspend fun delete(reading: ReadingData)

    @Query("SELECT * FROM readings")
    suspend fun getAllReadings(): List<ReadingData>
}
