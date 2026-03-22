package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.work.*

class SettingsActivity : BaseActivity() {

    private lateinit var cardLight: CardView
    private lateinit var cardDark: CardView
    private lateinit var etReminderDay: EditText
    private lateinit var btnSave: Button
    private var selectedTheme = "system"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        cardLight = findViewById(R.id.cardLight)
        cardDark = findViewById(R.id.cardDark)
        etReminderDay = findViewById(R.id.etReminderDay)
        btnSave = findViewById(R.id.btnSaveTheme)

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

            prefs.edit()
                .putString("theme", selectedTheme)
                .putInt("reminder_day", day)
                .apply()

            scheduleReminder(day)

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun updateThemeSelection() {
        val accentColor = getColor(R.color.accent)
        val lightCardColor = getColor(android.R.color.white)
        val darkCardColor = getColor(R.color.card_dark)

        cardLight.setCardBackgroundColor(
            if (selectedTheme == "light") accentColor else lightCardColor
        )
        cardDark.setCardBackgroundColor(
            if (selectedTheme == "dark") accentColor else darkCardColor
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
