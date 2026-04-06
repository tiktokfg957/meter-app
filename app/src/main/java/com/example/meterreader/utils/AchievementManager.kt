package com.example.meterreader.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.meterreader.data.DatabaseHelper
import com.example.meterreader.data.model.ReadingData
import java.text.SimpleDateFormat
import java.util.*

object AchievementManager {
    private const val PREFS_NAME = "achievements"
    private const val KEY_STREAK = "reading_streak"
    private const val KEY_LAST_MONTH = "last_reading_month"

    data class Achievement(
        val id: String,
        val name: String,
        val description: String,
        val condition: (DatabaseHelper, Long) -> Boolean,
        val rewardText: String
    )

    val allAchievements = listOf(
        Achievement("first_month", "Первый месяц", "Передавали показания целый месяц без пропусков",
            { db, objectId -> checkStreak(db, objectId, 1) }, "50 монет"),
        Achievement("three_months", "Три месяца подряд", "Три месяца без пропусков",
            { db, objectId -> checkStreak(db, objectId, 3) }, "150 монет"),
        Achievement("economy_1000", "Экономист", "Сэкономили 1000 рублей на электроэнергии",
            { db, objectId -> checkEconomy(db, objectId, 1000.0) }, "200 монет"),
        Achievement("guru", "Гуру ЖКХ", "Внесли 100 показаний",
            { db, objectId -> checkTotalReadings(db, objectId, 100) }, "500 монет")
    )

    private fun checkStreak(db: DatabaseHelper, objectId: Long, months: Int): Boolean {
        val prefs = db.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val streak = prefs.getInt(KEY_STREAK + "_" + objectId, 0)
        return streak >= months
    }

    private fun checkEconomy(db: DatabaseHelper, objectId: Long, amount: Double): Boolean {
        // Упрощённо: считаем общую сумму экономии (не реализовано, заглушка)
        return false
    }

    private fun checkTotalReadings(db: DatabaseHelper, objectId: Long, count: Int): Boolean {
        val meters = db.getAllMeters(objectId)
        var totalReadings = 0
        for (meter in meters) {
            totalReadings += db.getReadingsForMeter(meter.id).size
        }
        return totalReadings >= count
    }

    fun updateStreak(db: DatabaseHelper, objectId: Long) {
        val prefs = db.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val lastMonth = prefs.getString(KEY_LAST_MONTH + "_" + objectId, "")
        var streak = prefs.getInt(KEY_STREAK + "_" + objectId, 0)

        if (currentMonth != lastMonth) {
            // Проверяем, были ли показания в этом месяце
            val hasReadingThisMonth = hasReadingsInMonth(db, objectId, currentMonth)
            if (hasReadingThisMonth) {
                if (currentMonth.toInt() == (lastMonth?.toInt() ?: 0) + 1) {
                    streak++
                } else {
                    streak = 1
                }
            } else {
                streak = 0
            }
            prefs.edit()
                .putInt(KEY_STREAK + "_" + objectId, streak)
                .putString(KEY_LAST_MONTH + "_" + objectId, currentMonth)
                .apply()
        }
    }

    private fun hasReadingsInMonth(db: DatabaseHelper, objectId: Long, month: String): Boolean {
        val meters = db.getAllMeters(objectId)
        for (meter in meters) {
            val readings = db.getReadingsForMeter(meter.id)
            if (readings.any { it.date.startsWith(month) }) return true
        }
        return false
    }
}
