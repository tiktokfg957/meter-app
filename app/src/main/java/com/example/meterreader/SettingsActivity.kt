package com.example.meterreader

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.work.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var cardLight: CardView
    private lateinit var cardDark: CardView
    private lateinit var etReminderDay: EditText
    private lateinit var btnSave: Button
    private lateinit var prefs: SharedPreferences
    private var selectedTheme = "system"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        cardLight = findViewById(R.id.cardLight)
        cardDark = findViewById(R.id.cardDark)
        etReminderDay = findViewById(R.id.etReminderDay)
        btnSave = findViewById(R.id.btnSaveTheme)

        // Загружаем текущую тему
        val currentTheme = prefs.getString("theme", "system")
        selectedTheme = currentTheme ?: "system"
        updateThemeSelection()

        cardLight.setOnClickListener {
            selectedTheme = "light"
            updateThemeSelection()
        }

        cardDark.setOnClickListener {
            selectedTheme = "dark"
            updateThemeSelection()
        }

        val reminderDay = prefs.getInt("reminder_day", 1)
        etReminderDay.setText(reminderDay.toString())

        btnSave.setOnClickListener {
            val dayStr = etReminderDay.text.toString().trim()
            val day = if (dayStr.isNotEmpty()) dayStr.toInt() else 1

            prefs.edit().putString("theme", selectedTheme).putInt("reminder_day", day).apply()

            scheduleReminder(day)

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun updateThemeSelection() {
        // Обводим выбранную карточку рамкой
        val borderColor = getColor(android.R.color.holo_blue_dark)
        val defaultBorder = getColor(android.R.color.transparent)

        cardLight.setCardBackgroundColor(
            if (selectedTheme == "light") getColor(android.R.color.holo_blue_light)
            else getColor(android.R.color.white)
        )
        cardDark.setCardBackgroundColor(
            if (selectedTheme == "dark") getColor(android.R.color.holo_blue_light)
            else getColor(android.R.color.darker_gray)
        )
    }

    private fun scheduleReminder(day: Int) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, java.util.concurrent.TimeUnit.DAYS)
            .setInitialDelay(1, java.util.concurrent.TimeUnit.DAYS)
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Toast.makeText(this, "Напоминание установлено на $day число каждого месяца", Toast.LENGTH_LONG).show()
    }
}
