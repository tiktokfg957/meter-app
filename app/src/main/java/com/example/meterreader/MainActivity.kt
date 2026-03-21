package com.example.meterreader

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.opencsv.CSVWriter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnStats: Button
    private lateinit var btnGoals: Button
    private lateinit var btnExportCSV: Button
    private lateinit var btnExportExcel: Button
    private lateinit var btnSettings: Button
    private lateinit var tvTotalMonth: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "system")
        applyTheme(theme)

        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        btnStats = findViewById(R.id.btnStats)
        btnGoals = findViewById(R.id.btnGoals)
        btnExportCSV = findViewById(R.id.btnExportCSV)
        btnExportExcel = findViewById(R.id.btnExportExcel)
        btnSettings = findViewById(R.id.btnSettings)
        tvTotalMonth = findViewById(R.id.tvTotalMonth)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddMeterActivity::class.java))
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnGoals.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        btnExportCSV.setOnClickListener {
            exportToCSV()
        }

        btnExportExcel.setOnClickListener {
            exportToExcel()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadMeters()
    }

    override fun onResume() {
        super.onResume()
        loadMeters()
    }

    private fun applyTheme(theme: String?) {
        when (theme) {
            "dark" -> setTheme(R.style.AppTheme_Dark)
            "light" -> setTheme(R.style.AppTheme)
            else -> setTheme(R.style.AppTheme)
        }
    }

    private fun loadMeters() {
        val meters = dbHelper.getAllMeters()
        val adapter = MeterAdapter(
            meters = meters,
            onItemClick = { meter ->
                val intent = Intent(this, AddReadingActivity::class.java)
                intent.putExtra("meter_id", meter.id)
                startActivity(intent)
            },
            onItemLongClick = { meter ->
                val intent = Intent(this, AddMeterActivity::class.java)
                intent.putExtra("meter_id", meter.id)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        updateStats(meters)
        updateTotalForCurrentMonth()
    }

    private fun updateStats(meters: List<Meter>) {
        val readings = dbHelper.getAllReadings()
        var totalCost = 0f
        val consumptionByType = mutableMapOf<String, Float>()

        for (meter in meters) {
            val meterReadings = readings.filter { it.meterId == meter.id }
            if (meterReadings.isNotEmpty()) {
                val lastReading = meterReadings.last().value
                val firstReading = if (meterReadings.size > 1) meterReadings[meterReadings.size - 2].value else meter.initialReading
                val consumption = lastReading - firstReading
                val cost = consumption * meter.tariff
                totalCost += cost

                val type = meter.type
                consumptionByType[type] = consumptionByType.getOrDefault(type, 0f) + consumption
            }
        }

        // Можно также обновить TextView, если нужно, но у нас уже есть отдельное поле для итога за месяц
        // Оставлено для совместимости
    }

    private fun updateTotalForCurrentMonth() {
        val meters = dbHelper.getAllMeters()
        val readings = dbHelper.getAllReadings()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        var totalMonth = 0f

        for (meter in meters) {
            val monthReadings = readings.filter { it.meterId == meter.id && it.date.startsWith(currentMonth) }
            if (monthReadings.isNotEmpty()) {
                // Сортируем по дате, берём последнее показание за месяц и предыдущее (или начальное)
                val sorted = monthReadings.sortedBy { it.date }
                val last = sorted.last()
                val previous = if (sorted.size > 1) sorted[sorted.size - 2].value else meter.initialReading
                val diff = last.value - previous
                totalMonth += diff * meter.tariff
            }
        }

        tvTotalMonth.text = "%.2f ₽".format(totalMonth)
    }

    // Экспорт в CSV (без диалога, сразу сохраняет все данные)
    private fun exportToCSV() {
        try {
            val meters = dbHelper.getAllMeters()
            val readings = dbHelper.getAllReadings()

            val downloadsDir = getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath
            val file = File(downloadsDir, "meter_readings_${System.currentTimeMillis()}.csv")

            FileWriter(file).use { writer ->
                CSVWriter(writer).use { csvWriter ->
                    csvWriter.writeNext(arrayOf(
                        "ID счётчика", "Название", "Тип", "Дата", "Показания", "Разница", "Стоимость"
                    ))

                    for (meter in meters) {
                        val meterReadings = readings.filter { it.meterId == meter.id }
                        for (i in meterReadings.indices) {
                            val reading = meterReadings[i]
                            val diff = if (i > 0) {
                                reading.value - meterReadings[i-1].value
                            } else {
                                reading.value - meter.initialReading
                            }
                            val cost = diff * meter.tariff

                            csvWriter.writeNext(arrayOf(
                                meter.id.toString(),
                                meter.name,
                                meter.type,
                                reading.date,
                                reading.value.toString(),
                                diff.toString(),
                                "%.2f".format(cost)
                            ))
                        }
                    }
                }
            }

            Toast.makeText(this, "CSV сохранён: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Экспорт в Excel (без диалога)
    private fun exportToExcel() {
        try {
            val meters = dbHelper.getAllMeters()
            val readings = dbHelper.getAllReadings()

            val workbook: Workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Показания")

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("ID счётчика")
            headerRow.createCell(1).setCellValue("Название")
            headerRow.createCell(2).setCellValue("Тип")
            headerRow.createCell(3).setCellValue("Дата")
            headerRow.createCell(4).setCellValue("Показания")
            headerRow.createCell(5).setCellValue("Разница")
            headerRow.createCell(6).setCellValue("Стоимость")

            var rowNum = 1
            for (meter in meters) {
                val meterReadings = readings.filter { it.meterId == meter.id }
                for (i in meterReadings.indices) {
                    val reading = meterReadings[i]
                    val diff = if (i > 0) {
                        reading.value - meterReadings[i-1].value
                    } else {
                        reading.value - meter.initialReading
                    }
                    val cost = diff * meter.tariff

                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(meter.id.toDouble())
                    row.createCell(1).setCellValue(meter.name)
                    row.createCell(2).setCellValue(meter.type)
                    row.createCell(3).setCellValue(reading.date)
                    row.createCell(4).setCellValue(reading.value.toDouble())
                    row.createCell(5).setCellValue(diff.toDouble())
                    row.createCell(6).setCellValue(cost.toDouble())
                }
            }

            val downloadsDir = getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath
            val file = File(downloadsDir, "meter_readings_${System.currentTimeMillis()}.xlsx")

            file.outputStream().use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            Toast.makeText(this, "Excel сохранён: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
