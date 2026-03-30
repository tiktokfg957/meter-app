package com.example.meterreader

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AddReadingActivity : BaseActivity() {

    private lateinit var tvMeterInfo: TextView
    private lateinit var etValue: EditText
    private lateinit var btnSave: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private var meterId: Long = 0
    private lateinit var meter: Meter
    private var editingReading: Reading? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reading)

        dbHelper = DatabaseHelper(this)
        meterId = intent.getLongExtra("meter_id", 0)
        if (meterId == 0L) {
            Toast.makeText(this, "Ошибка: счётчик не выбран", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
            val reading = Reading(
                meterId = meterId,
                value = value,
                date = currentDate
            )
            dbHelper.insertReading(reading)
            Toast.makeText(this, "Показания сохранены", Toast.LENGTH_SHORT).show()
            showSuccessAnimation()
        } else {
            editingReading?.value = value
            editingReading?.date = currentDate
            dbHelper.updateReading(editingReading!!)
            Toast.makeText(this, "Показания обновлены", Toast.LENGTH_SHORT).show()
            editingReading = null
            btnSave.text = "Сохранить показания"
            showSuccessAnimation()
        }

        etValue.text.clear()
        loadReadings()
    }

    private fun showSuccessAnimation() {
        val checkmarkView = findViewById<ImageView>(R.id.checkmark_animation)
        if (checkmarkView != null) {
            val anim = AnimationUtils.loadAnimation(this, R.anim.checkmark)
            checkmarkView.visibility = View.VISIBLE
            checkmarkView.startAnimation(anim)
            Handler(Looper.getMainLooper()).postDelayed({
                checkmarkView.visibility = View.GONE
            }, 1000)
        }
    }

    private fun loadReadings() {
        val readings = dbHelper.getReadingsForMeter(meterId)
        val adapter = ReadingAdapter(
            readings = readings,
            initialReading = meter.initialReading,
            onItemClick = { reading ->
                editingReading = reading
                etValue.setText(reading.value.toString())
                btnSave.text = "Обновить"
            },
            onItemLongClick = { reading ->
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
