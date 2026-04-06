package com.example.meterreader

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meterreader.databinding.ActivitySettingsBinding
import com.example.meterreader.utils.ReminderManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Настройки"

        // === Настройка напоминаний ===
        val days = (1..31).map { it.toString() }.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReminderDay.adapter = adapter

        val prefs = getSharedPreferences("reminder_prefs", MODE_PRIVATE)
        val savedDay = prefs.getInt("reminder_day", -1)
        if (savedDay != -1) {
            binding.spinnerReminderDay.setSelection(savedDay - 1)
        }

        binding.btnSaveReminder.setOnClickListener {
            val selectedDay = binding.spinnerReminderDay.selectedItemPosition + 1
            prefs.edit().putInt("reminder_day", selectedDay).apply()
            ReminderManager.scheduleReminder(this)
            Toast.makeText(this, "Напоминание установлено на $selectedDay число", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
