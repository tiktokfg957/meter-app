package com.example.meterreader.data.database

import androidx.room.*
import com.example.meterreader.data.model.SupportMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportMessageDao {
    @Query("SELECT * FROM support_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<SupportMessage>>

    @Insert
    suspend fun insert(message: SupportMessage)

    @Delete
    suspend fun delete(message: SupportMessage)
}
