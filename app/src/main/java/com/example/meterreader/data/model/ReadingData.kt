package com.example.meterreader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class ReadingData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meterId: Long,
    val date: String,   // "yyyy-MM-dd"
    val value: Float
)
