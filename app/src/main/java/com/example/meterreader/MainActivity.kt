package com.example.meterreader

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.opencsv.CSVWriter

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnExport: Button
    private lateinit var btnSettings: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем сохранённую тему
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "system")
        applyTheme(theme)
        
        setContentView(R.layout.activity_main)
        
        dbHelper = DatabaseHelper(this)
        
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        btnExport = findViewById(R.id.btnExport)
        btnSettings = findViewById(R.id.btnSettings)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddMeterActivity::class.java))
        }
        
        btnExport.setOnClickListener {
            showExportDialog()
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
            else -> setTheme(R.style.AppTheme) // по умолчанию светлая
        }
    }
    
    private fun loadMeters() {
        val meters = dbHelper.getAllMeters()
        val adapter = MeterAdapter(meters) { meter ->
            // Клик по счётчику - показываем детали
            val intent = Intent(this, AddReadingActivity::class.java)
            intent.putExtra("meter_id", meter.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }
    
    private fun showExportDialog() {
        val options = arrayOf("CSV", "Excel (XLSX)")
        android.app.AlertDialog.Builder(this)
            .setTitle("Экспорт данных")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToCSV()
                    1 -> exportToExcel()
                }
            }
            .show()
    }
    
    private fun exportToCSV() {
        try {
            val meters = dbHelper.getAllMeters()
            val readings = dbHelper.getAllReadings()
            
            val downloadsDir = getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath
            val file = File(downloadsDir, "meter_readings_${System.currentTimeMillis()}.csv")
            
            FileWriter(file).use { writer ->
                CSVWriter(writer).use { csvWriter ->
                    // Заголовки
                    csvWriter.writeNext(arrayOf(
                        "ID счётчика", "Название", "Тип", "Дата", "Показания", "Разница"
                    ))
                    
                    // Данные
                    for (meter in meters) {
                        val meterReadings = readings.filter { it.meterId == meter.id }
                        for (i in meterReadings.indices) {
                            val reading = meterReadings[i]
                            val diff = if (i > 0) {
                                reading.value - meterReadings[i-1].value
                            } else {
                                reading.value - meter.initialReading
                            }
                            
                            csvWriter.writeNext(arrayOf(
                                meter.id.toString(),
                                meter.name,
                                meter.type,
                                reading.date,
                                reading.value.toString(),
                                diff.toString()
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
            
            // Заголовки
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("ID счётчика")
            headerRow.createCell(1).setCellValue("Название")
            headerRow.createCell(2).setCellValue("Тип")
            headerRow.createCell(3).setCellValue("Дата")
            headerRow.createCell(4).setCellValue("Показания")
            headerRow.createCell(5).setCellValue("Разница")
            
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
                    
                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(meter.id.toDouble())
                    row.createCell(1).setCellValue(meter.name)
                    row.createCell(2).setCellValue(meter.type)
                    row.createCell(3).setCellValue(reading.date)
                    row.createCell(4).setCellValue(reading.value.toDouble())
                    row.createCell(5).setCellValue(diff.toDouble())
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
