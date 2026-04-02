package com.example.meterreader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Float,
    val currentAmount: Float = 0f,
    val deadline: String? = null
)
