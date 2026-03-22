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
    private lateinit var btnTelegram: Button
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
        btnTelegram = findViewById(R.id.btnTelegram)
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

    private fun updateTotalForCurrentMonth() {
        val meters = dbHelper.getAllMeters()
        val readings = dbHelper.getAllReadings()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        var totalMonth = 0f

        for (meter in meters) {
            val monthReadings = readings.filter { it.meterId == meter.id && it.date.startsWith(currentMonth) }
            if (monthReadings.isNotEmpty()) {
                val sorted = monthReadings.sortedBy { it.date }
                val last = sorted.last()
                val previous = if (sorted.size > 1) sorted[sorted.size - 2].value else meter.initialReading
                val diff = last.value - previous
                totalMonth += diff * meter.tariff
            }
        }

        tvTotalMonth.text = "%.2f ₽".format(totalMonth)
    }

    // Методы экспорта (без изменений)
    private fun exportToCSV() { /* ... */ }
    private fun exportToExcel() { /* ... */ }
}
