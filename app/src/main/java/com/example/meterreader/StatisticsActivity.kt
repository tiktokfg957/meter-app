package com.example.meterreader

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : BaseActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvPeak: TextView
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        dbHelper = DatabaseHelper(this)
        tvPeak = findViewById(R.id.tvPeak)
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        loadStatistics()
    }

    private fun loadStatistics() {
        val meters = dbHelper.getAllMeters()
        val readings = dbHelper.getAllReadings()

        val monthlyExpenses = mutableMapOf<String, Float>()
        val categoryExpenses = mutableMapOf<String, Float>()

        for (meter in meters) {
            val meterReadings = readings.filter { it.meterId == meter.id }.sortedBy { it.date }
            var prevReading = meter.initialReading
            for (reading in meterReadings) {
                val diff = reading.value - prevReading
                val cost = diff * meter.tariff
                val month = reading.date.substring(0, 7)
                monthlyExpenses[month] = monthlyExpenses.getOrDefault(month, 0f) + cost
                categoryExpenses[meter.type] = categoryExpenses.getOrDefault(meter.type, 0f) + cost
                prevReading = reading.value
            }
        }

        val months = monthlyExpenses.keys.sorted()
        val entries = months.mapIndexed { index, month ->
            BarEntry(index.toFloat(), monthlyExpenses[month] ?: 0f)
        }

        val values = entries.map { it.y }
        val avg = if (values.isNotEmpty()) values.average().toFloat() else 0f
        val colors = values.map { value ->
            if (value > avg * 1.3f) Color.rgb(255, 200, 0) else ColorTemplate.MATERIAL_COLORS[0]
        }

        val dataSet = BarDataSet(entries, "Расходы по месяцам")
        dataSet.setColors(colors)
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()

        if (entries.isNotEmpty()) {
            val maxEntry = entries.maxByOrNull { it.y }
            val maxIndex = maxEntry?.x?.toInt() ?: 0
            val maxMonth = months[maxIndex]
            val maxValue = maxEntry?.y ?: 0f
            tvPeak.text = "Пик потребления: $maxMonth (%.2f ₽)".format(maxValue)
        } else {
            tvPeak.text = "Нет данных для определения пика"
        }

        val pieEntries = categoryExpenses.map { (category, amount) -> PieEntry(amount, category) }
        val pieDataSet = PieDataSet(pieEntries, "Доля категорий")
        pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.invalidate()
    }
}
