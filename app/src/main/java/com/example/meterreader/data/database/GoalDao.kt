package com.example.meterreader.data.database

import androidx.room.*
import com.example.meterreader.data.model.GoalData
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalData>>

    @Insert
    suspend fun insert(goal: GoalData)

    @Update
    suspend fun update(goal: GoalData)

    @Delete
    suspend fun delete(goal: GoalData)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalData?
}
