package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnStats: Button
    private lateinit var btnGoals: Button
    private lateinit var btnExportCSV: Button
    private lateinit var btnExportExcel: Button
    private lateinit var btnSettings: Button
    private lateinit var tvTotalMonth: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Кнопка Telegram-канала
        val btnTelegram = findViewById<Button>(R.id.btnTelegram)
        btnTelegram.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/zhku_app_news"))
            startActivity(intent)
        }

        loadMeters()
    }

    override fun onResume() {
        super.onResume()
        loadMeters()
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

        updateTotalForCurrentMonth()
    }

    // Исправленный метод расчёта итога за месяц
    private fun updateTotalForCurrentMonth() {
        val meters = dbHelper.getAllMeters()
        val readings = dbHelper.getAllReadings()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        var totalMonth = 0f

        for (meter in meters) {
            // Все показания счётчика по дате
            val allReadings = readings.filter { it.meterId == meter.id }.sortedBy { it.date }
            // Показания за текущий месяц
            val monthReadings = allReadings.filter { it.date.startsWith(currentMonth) }

            if (monthReadings.isNotEmpty()) {
                val lastOfMonth = monthReadings.last()
                // Последнее показание до начала текущего месяца (или начальное, если нет)
                val previous = allReadings.lastOrNull { it.date < currentMonth }
                val prevValue = previous?.value ?: meter.initialReading
                val diff = lastOfMonth.value - prevValue
                totalMonth += diff * meter.tariff
            }
        }

        tvTotalMonth.text = "%.2f ₽".format(totalMonth)
    }

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
