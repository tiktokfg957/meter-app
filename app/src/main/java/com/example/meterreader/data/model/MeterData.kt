package com.example.meterreader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meters")
data class MeterData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val objectId: Long,
    val name: String,
    val type: String,   // "Вода", "Свет", "Газ"
    val tariff: Float,
    val initialReading: Float
)
