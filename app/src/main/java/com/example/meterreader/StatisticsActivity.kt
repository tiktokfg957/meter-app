package com.example.meterreader

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvForecast: TextView
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        dbHelper = DatabaseHelper(this)

        tvForecast = findViewById(R.id.tvForecast)
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)

        loadStatistics()
    }

    private fun loadStatistics() {
        val meters = dbHelper.getAllMeters()
        val readings = dbHelper.getAllReadings()

        // Подготовим данные по месяцам
        val monthlyExpenses = mutableMapOf<String, Float>()
        val categoryExpenses = mutableMapOf<String, Float>()

        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for (meter in meters) {
            val meterReadings = readings.filter { it.meterId == meter.id }.sortedBy { it.date }
            var prevReading = meter.initialReading
            for (reading in meterReadings) {
                val diff = reading.value - prevReading
                val cost = diff * meter.tariff
                val month = reading.date.substring(0, 7) // yyyy-MM
                monthlyExpenses[month] = monthlyExpenses.getOrDefault(month, 0f) + cost
                categoryExpenses[meter.type] = categoryExpenses.getOrDefault(meter.type, 0f) + cost
                prevReading = reading.value
            }
        }

        // График расходов по месяцам (BarChart)
        val months = monthlyExpenses.keys.sorted()
        val entries = months.mapIndexed { index, month ->
            BarEntry(index.toFloat(), monthlyExpenses[month] ?: 0f)
        }
        val dataSet = BarDataSet(entries, "Расходы по месяцам")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()

        // Круговая диаграмма по категориям
        val pieEntries = categoryExpenses.map { (category, amount) ->
            PieEntry(amount, category)
        }
        val pieDataSet = PieDataSet(pieEntries, "Доля категорий")
        pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        val pieData = PieData(pieDataSet)
        pieChart.data = pieData
        pieChart.invalidate()

        // Прогноз на следующий месяц (простая экстраполяция)
        if (months.size >= 2) {
            val lastMonth = months.last()
            val prevMonth = months[months.size - 2]
            val lastAmount = monthlyExpenses[lastMonth] ?: 0f
            val prevAmount = monthlyExpenses[prevMonth] ?: 0f
            val change = lastAmount - prevAmount
            val forecast = lastAmount + change
            tvForecast.text = "Прогноз на следующий месяц: %.2f руб".format(forecast)
        } else {
            tvForecast.text = "Недостаточно данных для прогноза"
        }
    }
}
