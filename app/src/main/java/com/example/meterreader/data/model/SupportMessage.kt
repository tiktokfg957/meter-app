package com.example.meterreader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_messages")
data class SupportMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
