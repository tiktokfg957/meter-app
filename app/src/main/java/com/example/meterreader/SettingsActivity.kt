package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.work.*
import java.util.*

class SettingsActivity : BaseActivity() {

    private lateinit var cardLight: CardView
    private lateinit var cardDark: CardView
    private lateinit var etReminderDay: EditText
    private lateinit var btnSave: Button
    private lateinit var btnActivatePro: Button
    private lateinit var tvProStatus: TextView
    private var selectedTheme = "system"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        cardLight = findViewById(R.id.cardLight)
        cardDark = findViewById(R.id.cardDark)
        etReminderDay = findViewById(R.id.etReminderDay)
        btnSave = findViewById(R.id.btnSaveTheme)
        btnActivatePro = findViewById(R.id.btnActivatePro)
        tvProStatus = findViewById(R.id.tvProStatus)

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

        updateProStatus()

        btnActivatePro.setOnClickListener {
            activateProTrial()
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

    private fun updateProStatus() {
        val isPro = prefs.getBoolean("isPro", false)
        val expiry = prefs.getLong("proExpiryDate", 0)
        val now = System.currentTimeMillis()

        if (isPro || expiry > now) {
            val remaining = if (expiry > now) {
                val days = (expiry - now) / (1000 * 60 * 60 * 24)
                " (активен ещё ${days} дн.)"
            } else {
                " (активен)"
            }
            tvProStatus.text = "PRO активен$remaining"
            btnActivatePro.isEnabled = false
            btnActivatePro.text = "Уже активирован"
        } else {
            tvProStatus.text = "PRO не активирован"
            btnActivatePro.isEnabled = true
            btnActivatePro.text = "Активировать PRO (4 дня за подписку на VK)"
        }
    }

    private fun activateProTrial() {
        // Открываем VK для подписки
        val vkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club236967018"))
        startActivity(vkIntent)

        // Активируем пробный период на 4 дня
        val expiry = System.currentTimeMillis() + 4 * 24 * 60 * 60 * 1000L
        prefs.edit()
            .putBoolean("isPro", true)
            .putLong("proExpiryDate", expiry)
            .apply()

        Toast.makeText(this, "Пробный период PRO активирован на 4 дня! Спасибо за подписку.", Toast.LENGTH_LONG).show()

        updateProStatus()
    }
}
