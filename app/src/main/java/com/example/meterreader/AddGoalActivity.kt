package com.example.meterreader

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddGoalActivity : AppCompatActivity() {

    private lateinit var spinnerMeter: Spinner
    private lateinit var etTarget: EditText
    private lateinit var btnSave: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goal)

        dbHelper = DatabaseHelper(this)

        spinnerMeter = findViewById(R.id.spinnerMeter)
        etTarget = findViewById(R.id.etTarget)
        btnSave = findViewById(R.id.btnSaveGoal)

        // Загружаем счётчики для выпадающего списка
        val meters = dbHelper.getAllMeters()
        val meterNames = meters.map { it.name }.toMutableList()
        meterNames.add(0, "Общая цель (все счётчики)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, meterNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMeter.adapter = adapter

        btnSave.setOnClickListener {
            val targetStr = etTarget.text.toString().trim()
            if (targetStr.isEmpty()) {
                Toast.makeText(this, "Введите целевую сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val target = targetStr.toFloatOrNull() ?: 0f
            if (target <= 0) {
                Toast.makeText(this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedPosition = spinnerMeter.selectedItemPosition
            val meterId = if (selectedPosition == 0) 0L else meters[selectedPosition - 1].id

            val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val month = dateFormat.format(Date())

            val goal = Goal(
                meterId = meterId,
                targetAmount = target,
                currentAmount = 0f,
                month = month,
                achieved = false
            )
            dbHelper.insertGoal(goal)
            Toast.makeText(this, "Цель добавлена", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
