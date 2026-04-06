package com.example.meterreader.utils

import com.example.meterreader.data.DatabaseHelper
import com.example.meterreader.data.model.MeterData
import com.example.meterreader.data.model.ReadingData
import java.text.SimpleDateFormat
import java.util.*

object StatisticsHelper {

    data class ComparisonResult(
        val percentageChange: Int,
        val advice: String
    )

    fun getComparisonForMeter(meter: MeterData, currentMonthReadings: List<ReadingData>, previousMonthReadings: List<ReadingData>): ComparisonResult? {
        if (currentMonthReadings.isEmpty() || previousMonthReadings.isEmpty()) return null
        val currentTotal = currentMonthReadings.last().value - (currentMonthReadings.getOrNull(currentMonthReadings.size - 2)?.value ?: meter.initialReading)
        val previousTotal = previousMonthReadings.last().value - (previousMonthReadings.getOrNull(previousMonthReadings.size - 2)?.value ?: meter.initialReading)
        if (previousTotal == 0f) return null
        val change = ((currentTotal - previousTotal) / previousTotal * 100).toInt()
        val advice = when {
            change > 20 -> "Расход вырос на $change% по сравнению с прошлым месяцем. Проверьте, нет ли протечек или неисправностей."
            change < -20 -> "Отлично! Вы снизили расход на ${-change}%. Продолжайте в том же духе!"
            else -> "Расход стабилен. Чтобы сэкономить, попробуйте установить ресурсосберегающие насадки."
        }
        return ComparisonResult(change, advice)
    }

    fun getGeneralAdvice(dbHelper: DatabaseHelper, objectId: Long): String {
        val meters = dbHelper.getAllMeters(objectId)
        val readings = dbHelper.getAllReadings()
        val adviceList = mutableListOf<String>()
        for (meter in meters) {
            val meterReadings = readings.filter { it.meterId == meter.id }.sortedBy { it.date }
            if (meterReadings.size >= 2) {
                val last = meterReadings.last()
                val prev = meterReadings[meterReadings.size - 2]
                val diff = last.value - prev.value
                if (meter.type == "Вода" && diff > 10) {
                    adviceList.add("По счётчику воды резкий скачок. Возможно, течёт кран.")
                } else if (meter.type == "Свет" && diff > 200) {
                    adviceList.add("Электричество подорожало? Проверьте, не оставлены ли включёнными мощные приборы.")
                }
            }
        }
        return if (adviceList.isNotEmpty()) adviceList.joinToString("\n") else "Продолжайте в том же духе! У вас отличная экономия."
    }
}
