package com.example.meterreader

import android.content.Intent
import android.content.pm.PackageManager
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

        // Раздел "О приложении"
        val btnAbout = findViewById<Button>(R.id.btnAbout)
        btnAbout.setOnClickListener {
            showAboutDialog()
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
        val vkIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club236967018"))
        startActivity(vkIntent)

        val expiry = System.currentTimeMillis() + 4 * 24 * 60 * 60 * 1000L
        prefs.edit()
            .putBoolean("isPro", true)
            .putLong("proExpiryDate", expiry)
            .apply()

        Toast.makeText(this, "Пробный период PRO активирован на 4 дня! Спасибо за подписку.", Toast.LENGTH_LONG).show()

        updateProStatus()
    }

    private fun showAboutDialog() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }

        val message = """
            Версия: $versionName
            Разработчик: Пищихин Дмитрий
            
            Приложение для учёта показаний счётчиков воды, электричества и газа.
            Все данные хранятся локально на вашем устройстве.
            
            Ссылки:
            • Страница VK: https://vk.com/club236967018
            • Политика конфиденциальности: https://www.rustore.ru/catalog/app/com.example.meterreader/privacy
            • Поддержка: pisihindmitrij0@gmail.com
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("О приложении")
            .setMessage(message)
            .setPositiveButton("Поделиться") { _, _ ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Попробуйте приложение «Учёт ЖКХ» для учёта показаний счётчиков! Скачать в RuStore: https://www.rustore.ru/catalog/app/com.example.meterreader")
                }
                startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }
}
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

        // Выбор дня напоминания
        val days = (1..31).map { "$it" }.toTypedArray()
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
