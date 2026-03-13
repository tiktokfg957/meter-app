package com.example.meterreader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddReadingActivity : AppCompatActivity() {
    
    private lateinit var tvMeterInfo: TextView
    private lateinit var etValue: EditText
    private lateinit var btnSave: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private var meterId: Long = 0
    private lateinit var meter: Meter
    private var editingReading: Reading? = null // для редактирования
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reading)
        
        dbHelper = DatabaseHelper(this)
        meterId = intent.getLongExtra("meter_id", 0)
        meter = dbHelper.getMeter(meterId) ?: run {
            Toast.makeText(this, "Счётчик не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        tvMeterInfo = findViewById(R.id.tvMeterInfo)
        etValue = findViewById(R.id.etReadingValue)
        btnSave = findViewById(R.id.btnSaveReading)
        recyclerView = findViewById(R.id.recyclerViewReadings)
        
        tvMeterInfo.text = "${meter.name} (${meter.type})"
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        btnSave.setOnClickListener {
            saveReading()
        }
        
        loadReadings()
    }
    
    private fun saveReading() {
        val valueStr = etValue.text.toString().trim()
        if (valueStr.isEmpty()) {
            Toast.makeText(this, "Введите показания", Toast.LENGTH_SHORT).show()
            return
        }
        
        val value = valueStr.toFloatOrNull() ?: 0f
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        if (editingReading == null) {
            // Добавление нового
            val reading = Reading(
                meterId = meterId,
                value = value,
                date = currentDate
            )
            dbHelper.insertReading(reading)
            Toast.makeText(this, "Показания сохранены", Toast.LENGTH_SHORT).show()
        } else {
            // Обновление существующего
            editingReading?.value = value
            editingReading?.date = currentDate
            dbHelper.updateReading(editingReading!!)
            Toast.makeText(this, "Показания обновлены", Toast.LENGTH_SHORT).show()
            editingReading = null
            btnSave.text = "Сохранить показания"
        }
        
        etValue.text.clear()
        loadReadings()
    }
    
    private fun loadReadings() {
        val readings = dbHelper.getReadingsForMeter(meterId)
        // Исправлено: импорт ReadingAdapter уже есть, типы лямбд указаны явно
        val adapter = ReadingAdapter(
            readings = readings,
            initialReading = meter.initialReading,
            onItemClick = { reading: Reading ->  // явно указан тип
                editingReading = reading
                etValue.setText(reading.value.toString())
                btnSave.text = "Обновить"
            },
            onItemLongClick = { reading: Reading ->  // явно указан тип
                AlertDialog.Builder(this)
                    .setTitle("Удалить запись")
                    .setMessage("Удалить показания от ${reading.date}?")
                    .setPositiveButton("Да") { _, _ ->
                        dbHelper.deleteReading(reading.id)
                        loadReadings()
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter
    }
}
